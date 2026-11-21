package com.lumaglass.widgets.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity as actionStartIntentActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.lumaglass.widgets.MainActivity
import com.lumaglass.widgets.data.CuratedQuotes
import com.lumaglass.widgets.data.FontStylePreset
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun LumaWidgetContent(
    context: Context,
    config: WidgetConfig,
    darkText: Boolean,
    telemetry: DeviceTelemetry,
    glassBackgroundBitmap: Bitmap,
    photoBitmap: Bitmap?,
    dialOrRingBitmap: Bitmap?
) {
    val size = LocalSize.current
    val isWide = size.width >= 200.dp
    val isLarge = size.height >= 200.dp

    val primaryColor = if (darkText) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val secondaryColor = if (darkText) Color(0xB30F172A) else Color(0xB3F8FAFC)
    val pillBg = if (darkText) Color(0x240F172A) else Color(0x2EFFFFFF)
    val accentColor = Color(0xFF0EA5E9)

    val fontFamily = when (config.fontStyle) {
        FontStylePreset.SANS_CLEAN -> FontFamily.SansSerif
        FontStylePreset.SERIF_EDITORIAL -> FontFamily.Serif
        FontStylePreset.MONO_TECH -> FontFamily.Monospace
        FontStylePreset.ROUNDED_SOFT -> FontFamily.SansSerif
    }

    val openConfigIntent = Intent(context, MainActivity::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, config.appWidgetId)
        putExtra("template_id", config.templateId.name)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val params = actionParametersOf(
        WidgetActionParams.INSTANCE_ID to config.instanceId,
        WidgetActionParams.APP_WIDGET_ID to config.appWidgetId
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(config.cornerRadiusDp.dp)
            .clickable(actionStartIntentActivity(openConfigIntent)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(glassBackgroundBitmap),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxSize()
        )

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(if (isLarge) 18.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (config.templateId) {
                WidgetTemplateId.FROSTED_DIGITAL_CLOCK -> {
                    val now = LocalTime.now()
                    val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM"))
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = TextStyle(
                            color = ColorProvider(primaryColor),
                            fontSize = if (isWide) 40.sp else 30.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = dateStr.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.MINIMAL_FLIP_CLOCK -> {
                    val now = LocalTime.now()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FlipTile(now.format(DateTimeFormatter.ofPattern("HH")), pillBg, primaryColor, fontFamily)
                        Spacer(GlanceModifier.width(8.dp))
                        FlipTile(now.format(DateTimeFormatter.ofPattern("mm")), pillBg, primaryColor, fontFamily)
                    }
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.ANALOG_RING_CLOCK -> {
                    if (dialOrRingBitmap != null) {
                        Image(
                            provider = ImageProvider(dialOrRingBitmap),
                            contentDescription = "Analog Ring Clock",
                            modifier = GlanceModifier.size(if (isLarge) 128.dp else 74.dp)
                        )
                    }
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE • d MMM")),
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.VERTICAL_TYPO_CLOCK -> {
                    val now = LocalTime.now()
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("HH")),
                        style = TextStyle(
                            color = ColorProvider(primaryColor),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("mm")),
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.DAY_PROGRESS_BAR -> {
                    val now = LocalTime.now()
                    val minutesElapsed = now.hour * 60 + now.minute
                    val pct = ((minutesElapsed / 1440f) * 100f).roundToInt().coerceIn(0, 100)
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = TextStyle(
                                    color = ColorProvider(primaryColor),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            )
                            Text(
                                text = "DAY PROGRESS • $pct%",
                                style = TextStyle(
                                    color = ColorProvider(secondaryColor),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = fontFamily
                                )
                            )
                        }
                    }
                    Spacer(GlanceModifier.height(10.dp))
                    GlanceSegmentedBar(pct = pct, trackColor = pillBg, fillColor = accentColor)
                }

                WidgetTemplateId.CIRCULAR_BATTERY_RING -> {
                    Box(contentAlignment = Alignment.Center) {
                        if (dialOrRingBitmap != null) {
                            Image(
                                provider = ImageProvider(dialOrRingBitmap),
                                contentDescription = "Battery Ring",
                                modifier = GlanceModifier.size(if (isLarge) 112.dp else 74.dp)
                            )
                        }
                        Text(
                            text = "${telemetry.batteryPercent}%",
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        )
                    }
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = if (telemetry.isCharging) "CHARGING" else "BATTERY",
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.STORAGE_RAM_PILL -> {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = "STORAGE  ${telemetry.storageUsedGb.roundToInt()} / ${telemetry.storageTotalGb.roundToInt()} GB",
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        GlanceSegmentedBar(telemetry.storagePercent, pillBg, accentColor)
                        Spacer(GlanceModifier.height(8.dp))
                        Text(
                            text = "RAM  ${String.format("%.1f", telemetry.ramUsedGb)} / ${String.format("%.1f", telemetry.ramTotalGb)} GB",
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        GlanceSegmentedBar(telemetry.ramPercent, pillBg, Color(0xFFA78BFA))
                    }
                }

                WidgetTemplateId.QUICK_STATUS_CARD -> {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .clickable(actionRunCallback<RefreshWidgetAction>(params))
                    ) {
                        Text(
                            text = "DEVICE STATUS",
                            style = TextStyle(
                                color = ColorProvider(secondaryColor),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = "📶 ${telemetry.wifiSsidOrStatus}",
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "☀️ Brightness ${telemetry.brightnessPercent}%  •  🔋 ${telemetry.batteryPercent}%",
                            style = TextStyle(
                                color = ColorProvider(secondaryColor),
                                fontSize = 11.sp,
                                fontFamily = fontFamily
                            )
                        )
                    }
                }

                WidgetTemplateId.EXAM_COUNTDOWN -> {
                    val today = LocalDate.now()
                    val target = LocalDate.ofEpochDay(config.targetEpochDay)
                    val daysLeft = ChronoUnit.DAYS.between(today, target).coerceAtLeast(0L)
                    Text(
                        text = config.customTitle.uppercase(),
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = "$daysLeft",
                        style = TextStyle(
                            color = ColorProvider(primaryColor),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                    Text(
                        text = if (daysLeft == 1L) "DAY LEFT" else "DAYS LEFT",
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.HABIT_STREAK -> {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = config.habitName,
                            maxLines = 1,
                            style = TextStyle(
                                color = ColorProvider(secondaryColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "🔥 ${config.habitStreakCount}",
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Box(
                            modifier = GlanceModifier
                                .background(pillBg)
                                .cornerRadius(999.dp)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .clickable(actionRunCallback<IncrementHabitAction>(params))
                        ) {
                            Text(
                                text = "+1 Today",
                                style = TextStyle(
                                    color = ColorProvider(primaryColor),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            )
                        }
                    }
                }

                WidgetTemplateId.STICKY_NOTE -> {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = "QUICK NOTE",
                            style = TextStyle(
                                color = ColorProvider(accentColor),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = config.customNoteOrQuote,
                            maxLines = if (isLarge) 6 else 3,
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 13.sp,
                                fontFamily = fontFamily
                            )
                        )
                    }
                }

                WidgetTemplateId.YEAR_PROGRESS_DOTS -> {
                    val today = LocalDate.now()
                    val dayOfYear = today.dayOfYear
                    val totalDays = today.lengthOfYear()
                    val pct = ((dayOfYear.toFloat() / totalDays) * 100f).roundToInt()
                    val filledBlocks = (pct / 10).coerceIn(1, 10)
                    val gridString = buildString {
                        for (i in 1..10) {
                            append(if (i <= filledBlocks) "● " else "○ ")
                        }
                    }.trim()

                    Text(
                        text = "${today.year} PROGRESS • $pct%",
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    Text(
                        text = gridString,
                        style = TextStyle(
                            color = ColorProvider(primaryColor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = "Day $dayOfYear of $totalDays",
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.POLAROID_PHOTO -> {
                    if (photoBitmap != null) {
                        Image(
                            provider = ImageProvider(photoBitmap),
                            contentDescription = "Polaroid photo",
                            contentScale = ContentScale.Crop,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                                .cornerRadius(14.dp)
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    } else {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                                .background(pillBg)
                                .cornerRadius(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap to pick photo",
                                style = TextStyle(
                                    color = ColorProvider(secondaryColor),
                                    fontSize = 11.sp,
                                    fontFamily = fontFamily
                                )
                            )
                        }
                        Spacer(GlanceModifier.height(6.dp))
                    }
                    Text(
                        text = config.customTitle.ifBlank { "Captured Moment" },
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(primaryColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = fontFamily
                        )
                    )
                }

                WidgetTemplateId.GLASS_PHOTO_CAROUSEL -> {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionRunCallback<CyclePhotoAction>(params)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (photoBitmap != null) {
                            Image(
                                provider = ImageProvider(photoBitmap),
                                contentDescription = "Carousel photo",
                                contentScale = ContentScale.Crop,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .defaultWeight()
                                    .cornerRadius(16.dp)
                            )
                        } else {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .defaultWeight()
                                    .background(pillBg)
                                    .cornerRadius(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Add Photos in Editor",
                                    style = TextStyle(
                                        color = ColorProvider(secondaryColor),
                                        fontSize = 12.sp,
                                        fontFamily = fontFamily
                                    )
                                )
                            }
                        }
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = "Tap photo to cycle →",
                            style = TextStyle(
                                color = ColorProvider(secondaryColor),
                                fontSize = 10.sp,
                                fontFamily = fontFamily
                            )
                        )
                    }
                }

                WidgetTemplateId.MINIMAL_QUOTE -> {
                    val quotePair = CuratedQuotes.items[config.quoteIndex % CuratedQuotes.items.size]
                    val displayQuote = if (config.customNoteOrQuote.isNotBlank() &&
                        config.customNoteOrQuote != WidgetConfig("temp").customNoteOrQuote
                    ) {
                        config.customNoteOrQuote
                    } else {
                        quotePair.first
                    }
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionRunCallback<CycleQuoteAction>(params)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "“$displayQuote”",
                            maxLines = if (isLarge) 5 else 3,
                            style = TextStyle(
                                color = ColorProvider(primaryColor),
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                fontFamily = fontFamily
                            )
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = "— ${quotePair.second} (tap for next)",
                            maxLines = 1,
                            style = TextStyle(
                                color = ColorProvider(secondaryColor),
                                fontSize = 10.sp,
                                fontFamily = fontFamily
                            )
                        )
                    }
                }

                WidgetTemplateId.QUICK_LAUNCH_DOCK -> {
                    Text(
                        text = "QUICK LAUNCH",
                        style = TextStyle(
                            color = ColorProvider(secondaryColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    val labels = listOf("Phone", "Msgs", "Web", "Cam")
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        labels.forEachIndexed { idx, label ->
                            val pkg = config.favoritePackages.getOrNull(idx)
                            val launchIntent = pkg?.let { context.packageManager.getLaunchIntentForPackage(it) }
                            val modifier = GlanceModifier
                                .background(pillBg)
                                .cornerRadius(14.dp)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                            val clickMod = if (launchIntent != null) {
                                modifier.clickable(actionStartIntentActivity(launchIntent))
                            } else {
                                modifier.clickable(actionStartActivity<MainActivity>())
                            }
                            Box(modifier = clickMod, contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        color = ColorProvider(primaryColor),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFamily
                                    )
                                )
                            }
                            if (idx < labels.lastIndex) {
                                Spacer(GlanceModifier.width(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipTile(
    digits: String,
    tileBg: Color,
    textColor: Color,
    fontFamily: FontFamily
) {
    Box(
        modifier = GlanceModifier
            .background(tileBg)
            .cornerRadius(14.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digits,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
        )
    }
}

@Composable
private fun GlanceSegmentedBar(
    pct: Int,
    trackColor: Color,
    fillColor: Color
) {
    val filledSegments = ((pct / 100f) * 10f).roundToInt().coerceIn(1, 10)
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..10) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(7.dp)
                    .background(if (i <= filledSegments) fillColor else trackColor)
                    .cornerRadius(999.dp)
            ) {}
            if (i < 10) {
                Spacer(GlanceModifier.width(3.dp))
            }
        }
    }
}
