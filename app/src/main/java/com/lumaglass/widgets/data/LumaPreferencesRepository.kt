package com.lumaglass.widgets.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

val Context.lumaDataStore: DataStore<Preferences> by preferencesDataStore(name = "lumaglass_prefs")

class LumaPreferencesRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private object Keys {
        val IS_PRO_UNLOCKED = booleanPreferencesKey("is_pro_unlocked")
        val REWARDED_UNLOCKS = stringSetPreferencesKey("rewarded_unlocked_templates")
        val FAVORITE_TEMPLATES = stringSetPreferencesKey("favorite_templates")
        val WIDGET_CONFIGS_JSON = stringPreferencesKey("widget_configs_json")
        val SELECTED_WALLPAPER_INDEX = stringPreferencesKey("selected_wallpaper_id")
        val SELECTED_APP_THEME_ID = stringPreferencesKey("selected_app_theme_id")
        val WIDGET_SAVE_COUNT = intPreferencesKey("widget_save_count")
        val HAS_SHOWN_REVIEW_PROMPT = booleanPreferencesKey("has_shown_review_prompt")
    }

    val isProUnlocked: Flow<Boolean> = context.lumaDataStore.data.map { prefs ->
        prefs[Keys.IS_PRO_UNLOCKED] ?: false
    }

    val rewardedUnlockedTemplates: Flow<Set<WidgetTemplateId>> = context.lumaDataStore.data.map { prefs ->
        val raw = prefs[Keys.REWARDED_UNLOCKS] ?: emptySet()
        raw.mapNotNull { name ->
            runCatching { WidgetTemplateId.valueOf(name) }.getOrNull()
        }.toSet()
    }

    val favoriteTemplates: Flow<Set<WidgetTemplateId>> = context.lumaDataStore.data.map { prefs ->
        val raw = prefs[Keys.FAVORITE_TEMPLATES] ?: emptySet()
        raw.mapNotNull { name ->
            runCatching { WidgetTemplateId.valueOf(name) }.getOrNull()
        }.toSet()
    }

    val configuredWidgets: Flow<List<WidgetConfig>> = context.lumaDataStore.data.map { prefs ->
        decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON])
            .values
            .sortedByDescending { it.lastUpdatedEpochMillis }
    }

    val selectedWallpaperId: Flow<String> = context.lumaDataStore.data.map { prefs ->
        prefs[Keys.SELECTED_WALLPAPER_INDEX] ?: "aurora"
    }

    val selectedAppThemeId: Flow<String> = context.lumaDataStore.data.map { prefs ->
        prefs[Keys.SELECTED_APP_THEME_ID] ?: AppColorTheme.PIXEL_BLUE.id
    }

    suspend fun setSelectedWallpaperId(id: String) {
        context.lumaDataStore.edit { prefs ->
            prefs[Keys.SELECTED_WALLPAPER_INDEX] = id
        }
    }

    suspend fun setSelectedAppThemeId(id: String) {
        context.lumaDataStore.edit { prefs ->
            prefs[Keys.SELECTED_APP_THEME_ID] = id
        }
    }

    suspend fun setProUnlocked(unlocked: Boolean) {
        context.lumaDataStore.edit { prefs ->
            prefs[Keys.IS_PRO_UNLOCKED] = unlocked
        }
    }

    suspend fun unlockTemplateViaRewardedAd(templateId: WidgetTemplateId) {
        context.lumaDataStore.edit { prefs ->
            val current = prefs[Keys.REWARDED_UNLOCKS] ?: emptySet()
            prefs[Keys.REWARDED_UNLOCKS] = current + templateId.name
        }
    }

    suspend fun toggleFavoriteTemplate(templateId: WidgetTemplateId) {
        context.lumaDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_TEMPLATES] ?: emptySet()
            prefs[Keys.FAVORITE_TEMPLATES] = if (templateId.name in current) {
                current - templateId.name
            } else {
                current + templateId.name
            }
        }
    }

    suspend fun incrementSaveCountAndCheckReviewPrompt(): Boolean {
        var shouldPrompt = false
        context.lumaDataStore.edit { prefs ->
            val nextCount = (prefs[Keys.WIDGET_SAVE_COUNT] ?: 0) + 1
            prefs[Keys.WIDGET_SAVE_COUNT] = nextCount
            val alreadyShown = prefs[Keys.HAS_SHOWN_REVIEW_PROMPT] ?: false
            if (nextCount >= 2 && !alreadyShown) {
                shouldPrompt = true
                prefs[Keys.HAS_SHOWN_REVIEW_PROMPT] = true
            }
        }
        return shouldPrompt
    }

    suspend fun isTemplateUnlocked(templateId: WidgetTemplateId): Boolean {
        if (templateId.isFreeByDefault) return true
        val prefs = context.lumaDataStore.data.first()
        if (prefs[Keys.IS_PRO_UNLOCKED] == true) return true
        val unlocked = prefs[Keys.REWARDED_UNLOCKS] ?: emptySet()
        return templateId.name in unlocked
    }

    suspend fun saveWidgetConfig(config: WidgetConfig) {
        val updated = config.copy(lastUpdatedEpochMillis = System.currentTimeMillis())
        context.lumaDataStore.edit { prefs ->
            val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON]).toMutableMap()
            map[updated.instanceId] = updated
            prefs[Keys.WIDGET_CONFIGS_JSON] = json.encodeToString(map)
        }
    }

    suspend fun getWidgetConfigById(instanceId: String): WidgetConfig? {
        val prefs = context.lumaDataStore.data.first()
        return decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON])[instanceId]
    }

    /**
     * Resolves the configuration bound to a pinned launcher appWidgetId, or falls back to
     * the most recently customized template in the matching category so pinning works seamlessly.
     */
    suspend fun getConfigForAppWidget(
        appWidgetId: Int,
        category: WidgetCategory
    ): WidgetConfig {
        val prefs = context.lumaDataStore.data.first()
        val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON])
        val directKey = "appWidget_$appWidgetId"
        map[directKey]?.let { return it }

        val recentInCategory = map.values
            .filter { it.templateId.category == category }
            .maxByOrNull { it.lastUpdatedEpochMillis }

        return recentInCategory?.copy(
            instanceId = directKey,
            appWidgetId = appWidgetId
        ) ?: WidgetConfig(
            instanceId = directKey,
            appWidgetId = appWidgetId,
            templateId = WidgetTemplateId.defaultForCategory(category)
        )
    }

    suspend fun deleteWidgetConfig(instanceId: String) {
        var orphanCandidatePaths: Set<String> = emptySet()
        var activePhotoPaths: Set<String> = emptySet()

        context.lumaDataStore.edit { prefs ->
            val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON]).toMutableMap()
            val removed = map.remove(instanceId)
            if (removed != null) {
                orphanCandidatePaths = buildSet {
                    removed.photoUri?.let { add(it) }
                    addAll(removed.photoCarouselUris)
                }
                activePhotoPaths = map.values.flatMap { cfg ->
                    buildList {
                        cfg.photoUri?.let { add(it) }
                        addAll(cfg.photoCarouselUris)
                    }
                }.toSet()
            }
            prefs[Keys.WIDGET_CONFIGS_JSON] = json.encodeToString(map)
        }

        // Safely clean up internal downscaled photo files that are no longer referenced by any widget
        (orphanCandidatePaths - activePhotoPaths).forEach { path ->
            runCatching {
                val file = File(path)
                if (file.exists() && file.absolutePath.contains("widget_photos")) {
                    file.delete()
                }
            }
        }
    }

    private suspend fun updateOrInitWidget(
        instanceId: String,
        fallbackAppWidgetId: Int,
        fallbackTemplate: WidgetTemplateId,
        transform: (WidgetConfig) -> WidgetConfig
    ) {
        context.lumaDataStore.edit { prefs ->
            val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON]).toMutableMap()
            val key = when {
                map.containsKey(instanceId) -> instanceId
                fallbackAppWidgetId > 0 && map.containsKey("appWidget_$fallbackAppWidgetId") -> "appWidget_$fallbackAppWidgetId"
                else -> map.values
                    .filter { it.templateId == fallbackTemplate }
                    .maxByOrNull { it.lastUpdatedEpochMillis }
                    ?.instanceId
                    ?: if (fallbackAppWidgetId > 0) "appWidget_$fallbackAppWidgetId" else instanceId
            }
            val existing = map[key] ?: WidgetConfig(
                instanceId = key,
                appWidgetId = fallbackAppWidgetId,
                templateId = fallbackTemplate
            )
            map[key] = transform(existing).copy(
                lastUpdatedEpochMillis = System.currentTimeMillis()
            )
            prefs[Keys.WIDGET_CONFIGS_JSON] = json.encodeToString(map)
        }
    }

    suspend fun incrementHabitStreak(instanceId: String, fallbackAppWidgetId: Int) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.HABIT_STREAK) { existing ->
            existing.copy(habitStreakCount = existing.habitStreakCount + 1)
        }
    }

    suspend fun incrementWaterGlasses(instanceId: String, fallbackAppWidgetId: Int) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.DAILY_WATER_TRACKER) { existing ->
            val nextCount = if (existing.waterGlassesCount >= existing.waterDailyGoal + 4) {
                1
            } else {
                existing.waterGlassesCount + 1
            }
            existing.copy(waterGlassesCount = nextCount)
        }
    }

    suspend fun incrementFocusSessions(instanceId: String, fallbackAppWidgetId: Int) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.MINIMAL_FOCUS_TIMER) { existing ->
            existing.copy(focusSessionsCount = existing.focusSessionsCount + 1)
        }
    }

    suspend fun toggleTodoItem(instanceId: String, fallbackAppWidgetId: Int, itemIndex: Int) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.TODO_CHECKLIST) { existing ->
            val bit = 1 shl itemIndex.coerceIn(0, 5)
            existing.copy(todoCompletedMask = existing.todoCompletedMask xor bit)
        }
    }

    suspend fun incrementCaffeineCups(instanceId: String, fallbackAppWidgetId: Int) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.CAFFEINE_TRACKER) { existing ->
            val next = if (existing.caffeineCupsCount >= existing.caffeineDailyLimit + 2) {
                0
            } else {
                existing.caffeineCupsCount + 1
            }
            existing.copy(caffeineCupsCount = next)
        }
    }

    suspend fun incrementDailySpend(instanceId: String, fallbackAppWidgetId: Int, deltaAmount: Int = 100) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.DAILY_BUDGET_PILL) { existing ->
            val next = if (existing.dailySpendAmount >= existing.dailyBudgetLimit * 2) {
                0
            } else {
                existing.dailySpendAmount + deltaAmount
            }
            existing.copy(dailySpendAmount = next)
        }
    }

    suspend fun incrementStepsCount(instanceId: String, fallbackAppWidgetId: Int, deltaSteps: Int = 500) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.DAILY_STEP_GOAL) { existing ->
            val next = if (existing.stepsCount >= existing.stepsDailyGoal + 4000) {
                500
            } else {
                existing.stepsCount + deltaSteps
            }
            existing.copy(stepsCount = next)
        }
    }

    suspend fun cycleQuote(instanceId: String, fallbackAppWidgetId: Int) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.MINIMAL_QUOTE) { existing ->
            val nextIdx = (existing.quoteIndex + 1) % CuratedQuotes.items.size
            existing.copy(quoteIndex = nextIdx)
        }
    }

    suspend fun cycleCarouselPhoto(instanceId: String, fallbackAppWidgetId: Int) {
        updateOrInitWidget(instanceId, fallbackAppWidgetId, WidgetTemplateId.GLASS_PHOTO_CAROUSEL) { existing ->
            val count = existing.photoCarouselUris.size.coerceAtLeast(1)
            existing.copy(carouselIndex = (existing.carouselIndex + 1) % count)
        }
    }

    private fun decodeWidgetMap(raw: String?): Map<String, WidgetConfig> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, WidgetConfig>>(raw)
        }.getOrDefault(emptyMap())
    }
}
