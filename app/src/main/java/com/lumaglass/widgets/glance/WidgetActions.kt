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
        ProductivityGlanceWidget().updateAll(context)
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
    ClockGlanceWidget().updateAll(context)
    BatteryGlanceWidget().updateAll(context)
    ProductivityGlanceWidget().updateAll(context)
    PhotoGlanceWidget().updateAll(context)
}
