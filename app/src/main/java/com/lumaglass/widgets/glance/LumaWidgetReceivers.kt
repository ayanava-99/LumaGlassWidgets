package com.lumaglass.widgets.glance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.lumaglass.widgets.data.LumaPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseLumaWidgetReceiver : GlanceAppWidgetReceiver() {
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val pendingResult = goAsync()
        val repo = LumaPreferencesRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { id ->
                    repo.deleteWidgetConfig("appWidget_$id")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class ClockWidgetReceiver : BaseLumaWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ClockGlanceWidget()
}

class BatteryWidgetReceiver : BaseLumaWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryGlanceWidget()
}

class ProductivityWidgetReceiver : BaseLumaWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ProductivityGlanceWidget()
}

class PhotoWidgetReceiver : BaseLumaWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PhotoGlanceWidget()
}

class MinimalWidgetReceiver : BaseLumaWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MinimalGlanceWidget()
}

class AbstractWidgetReceiver : BaseLumaWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AbstractGlanceWidget()
}

class SystemEventWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                refreshAllCategoryWidgets(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
