package com.lumaglass.widgets.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.lumaglass.widgets.data.LumaPreferencesRepository

object WidgetActionParams {
    val INSTANCE_ID = ActionParameters.Key<String>("instance_id")
    val APP_WIDGET_ID = ActionParameters.Key<Int>("app_widget_id")
    val TODO_INDEX = ActionParameters.Key<Int>("todo_index")
}

class IncrementHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.incrementHabitStreak(instanceId, appWidgetId)
        refreshAllCategoryWidgets(context)
    }
}

class IncrementWaterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.incrementWaterGlasses(instanceId, appWidgetId)
        refreshAllCategoryWidgets(context)
    }
}

class IncrementFocusAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.incrementFocusSessions(instanceId, appWidgetId)
        refreshAllCategoryWidgets(context)
    }
}

class ToggleTodoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        val todoIndex = parameters[WidgetActionParams.TODO_INDEX] ?: 0
        repo.toggleTodoItem(instanceId, appWidgetId, todoIndex)
        refreshAllCategoryWidgets(context)
    }
}

class IncrementCaffeineAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.incrementCaffeineCups(instanceId, appWidgetId)
        refreshAllCategoryWidgets(context)
    }
}

class IncrementSpendAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.incrementDailySpend(instanceId, appWidgetId, 100)
        refreshAllCategoryWidgets(context)
    }
}

class IncrementStepsAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.incrementStepsCount(instanceId, appWidgetId, 500)
        refreshAllCategoryWidgets(context)
    }
}

class CycleQuoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.cycleQuote(instanceId, appWidgetId)
        refreshAllCategoryWidgets(context)
    }
}

class CyclePhotoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val repo = LumaPreferencesRepository(context)
        val appWidgetId = parameters[WidgetActionParams.APP_WIDGET_ID]
            ?: runCatching { GlanceAppWidgetManager(context).getAppWidgetId(glanceId) }.getOrDefault(-1)
        val instanceId = parameters[WidgetActionParams.INSTANCE_ID] ?: "appWidget_$appWidgetId"
        repo.cycleCarouselPhoto(instanceId, appWidgetId)
        refreshAllCategoryWidgets(context)
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        refreshAllCategoryWidgets(context)
    }
}

suspend fun refreshAllCategoryWidgets(context: Context) {
    MinimalGlanceWidget().updateAll(context)
    AbstractGlanceWidget().updateAll(context)
    ClockGlanceWidget().updateAll(context)
    BatteryGlanceWidget().updateAll(context)
    ProductivityGlanceWidget().updateAll(context)
    PhotoGlanceWidget().updateAll(context)
}
