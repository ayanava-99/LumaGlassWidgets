package com.lumaglass.widgets.glance

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.lumaglass.widgets.data.BitmapHelper
import com.lumaglass.widgets.data.LumaPreferencesRepository
import com.lumaglass.widgets.data.WidgetCategory
import com.lumaglass.widgets.data.WidgetTemplateId
import java.time.LocalTime

abstract class BaseLumaGlanceWidget(
    private val defaultCategory: WidgetCategory
) : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp), // 2x2 Compact
            DpSize(250.dp, 110.dp), // 4x2 Wide
            DpSize(250.dp, 250.dp)  // 4x4 Large
        )
    )

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = LumaPreferencesRepository(context)
        val appWidgetId = runCatching {
            GlanceAppWidgetManager(context).getAppWidgetId(id)
        }.getOrDefault(-1)

        val config = repository.getConfigForAppWidget(appWidgetId, defaultCategory)
        val isSystemDark = (context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val darkText = BitmapHelper.isDarkTextSurface(context, config, isSystemDark)

        // Pre-render liquid glass card bitmap & downscale any user photo BEFORE provideContent
        // so SizeMode.Responsive never decodes bitmaps inside composition or causes launcher OOM.
        val glassBackgroundBitmap = BitmapHelper.renderGlassSurfaceBitmap(
            context = context,
            config = config,
            isSystemDark = isSystemDark,
            widthPx = 360,
            heightPx = 360
        )

        val telemetry = DeviceTelemetryReader.read(context)
        val now = LocalTime.now()

        val photoBitmap = when (config.templateId) {
            WidgetTemplateId.POLAROID_PHOTO -> {
                BitmapHelper.loadSafeWidgetBitmap(context, config.photoUri, maxDim = 420)
            }
            WidgetTemplateId.GLASS_PHOTO_CAROUSEL -> {
                val uris = config.photoCarouselUris
                val activeUri = if (uris.isNotEmpty()) {
                    uris[config.carouselIndex % uris.size]
                } else {
                    config.photoUri
                }
                BitmapHelper.loadSafeWidgetBitmap(context, activeUri, maxDim = 420)
            }
            else -> null
        }

        val dialOrRingBitmap = when (config.templateId) {
            WidgetTemplateId.ANALOG_RING_CLOCK -> {
                BitmapHelper.renderAnalogClockDialBitmap(
                    hour = now.hour,
                    minute = now.minute,
                    darkText = darkText
                )
            }
            WidgetTemplateId.CIRCULAR_BATTERY_RING -> {
                BitmapHelper.renderBatteryRingBitmap(
                    levelPercent = telemetry.batteryPercent,
                    isCharging = telemetry.isCharging,
                    darkText = darkText
                )
            }
            else -> null
        }

        provideContent {
            LumaWidgetContent(
                context = context,
                config = config,
                darkText = darkText,
                telemetry = telemetry,
                glassBackgroundBitmap = glassBackgroundBitmap,
                photoBitmap = photoBitmap,
                dialOrRingBitmap = dialOrRingBitmap
            )
        }
    }
}

class ClockGlanceWidget : BaseLumaGlanceWidget(WidgetCategory.CLOCKS)
class BatteryGlanceWidget : BaseLumaGlanceWidget(WidgetCategory.BATTERY)
class ProductivityGlanceWidget : BaseLumaGlanceWidget(WidgetCategory.PRODUCTIVITY)
class PhotoGlanceWidget : BaseLumaGlanceWidget(WidgetCategory.PHOTO)
