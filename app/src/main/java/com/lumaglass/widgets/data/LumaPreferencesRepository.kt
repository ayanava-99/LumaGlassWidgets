package com.lumaglass.widgets.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.lumaDataStore: DataStore<Preferences> by preferencesDataStore(name = "lumaglass_prefs")

class LumaPreferencesRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private object Keys {
        val IS_PRO_UNLOCKED = booleanPreferencesKey("is_pro_unlocked")
        val REWARDED_UNLOCKS = stringSetPreferencesKey("rewarded_unlocked_templates")
        val WIDGET_CONFIGS_JSON = stringPreferencesKey("widget_configs_json")
        val SELECTED_WALLPAPER_INDEX = stringPreferencesKey("selected_wallpaper_id")
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

    val configuredWidgets: Flow<List<WidgetConfig>> = context.lumaDataStore.data.map { prefs ->
        decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON])
            .values
            .sortedByDescending { it.lastUpdatedEpochMillis }
    }

    val selectedWallpaperId: Flow<String> = context.lumaDataStore.data.map { prefs ->
        prefs[Keys.SELECTED_WALLPAPER_INDEX] ?: "aurora"
    }

    suspend fun setSelectedWallpaperId(id: String) {
        context.lumaDataStore.edit { prefs ->
            prefs[Keys.SELECTED_WALLPAPER_INDEX] = id
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
        context.lumaDataStore.edit { prefs ->
            val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON]).toMutableMap()
            map.remove(instanceId)
            prefs[Keys.WIDGET_CONFIGS_JSON] = json.encodeToString(map)
        }
    }

    suspend fun incrementHabitStreak(instanceId: String, fallbackAppWidgetId: Int) {
        context.lumaDataStore.edit { prefs ->
            val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON]).toMutableMap()
            val key = if (map.containsKey(instanceId)) instanceId else "appWidget_$fallbackAppWidgetId"
            val existing = map[key] ?: WidgetConfig(
                instanceId = key,
                appWidgetId = fallbackAppWidgetId,
                templateId = WidgetTemplateId.HABIT_STREAK
            )
            map[key] = existing.copy(
                habitStreakCount = existing.habitStreakCount + 1,
                lastUpdatedEpochMillis = System.currentTimeMillis()
            )
            prefs[Keys.WIDGET_CONFIGS_JSON] = json.encodeToString(map)
        }
    }

    suspend fun cycleQuote(instanceId: String, fallbackAppWidgetId: Int) {
        context.lumaDataStore.edit { prefs ->
            val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON]).toMutableMap()
            val key = if (map.containsKey(instanceId)) instanceId else "appWidget_$fallbackAppWidgetId"
            val existing = map[key] ?: WidgetConfig(
                instanceId = key,
                appWidgetId = fallbackAppWidgetId,
                templateId = WidgetTemplateId.MINIMAL_QUOTE
            )
            val nextIdx = (existing.quoteIndex + 1) % CuratedQuotes.items.size
            map[key] = existing.copy(
                quoteIndex = nextIdx,
                lastUpdatedEpochMillis = System.currentTimeMillis()
            )
            prefs[Keys.WIDGET_CONFIGS_JSON] = json.encodeToString(map)
        }
    }

    suspend fun cycleCarouselPhoto(instanceId: String, fallbackAppWidgetId: Int) {
        context.lumaDataStore.edit { prefs ->
            val map = decodeWidgetMap(prefs[Keys.WIDGET_CONFIGS_JSON]).toMutableMap()
            val key = if (map.containsKey(instanceId)) instanceId else "appWidget_$fallbackAppWidgetId"
            val existing = map[key] ?: WidgetConfig(
                instanceId = key,
                appWidgetId = fallbackAppWidgetId,
                templateId = WidgetTemplateId.GLASS_PHOTO_CAROUSEL
            )
            val count = existing.photoCarouselUris.size.coerceAtLeast(1)
            map[key] = existing.copy(
                carouselIndex = (existing.carouselIndex + 1) % count,
                lastUpdatedEpochMillis = System.currentTimeMillis()
            )
            prefs[Keys.WIDGET_CONFIGS_JSON] = json.encodeToString(map)
        }
    }

    private fun decodeWidgetMap(raw: String?): Map<String, WidgetConfig> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, WidgetConfig>>(raw)
        }.getOrDefault(emptyMap())
    }
}
