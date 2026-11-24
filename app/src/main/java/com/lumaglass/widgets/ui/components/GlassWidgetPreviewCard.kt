package com.lumaglass.widgets.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.data.BitmapHelper
import com.lumaglass.widgets.data.CuratedQuotes
import com.lumaglass.widgets.data.DockAppPresets
import com.lumaglass.widgets.data.FontStylePreset
import com.lumaglass.widgets.data.GlassSurfaceTexture
import com.lumaglass.widgets.data.GlassTintPreset
import com.lumaglass.widgets.data.MoonPhaseCalculator
import com.lumaglass.widgets.data.ThemeMode
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.data.WorldClockCities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Renders an interactive, high-fidelity Compose preview of any of the 40 LumaGlass widgets.
 */
@Composable
fun GlassWidgetPreviewCard(
    config: WidgetConfig,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false
) {
    val context = LocalContext.current
    val isSysDark = isSystemInDarkTheme()

    val baseColor = resolveComposeGlassColor(config, isSysDark)
    val darkText = BitmapHelper.isDarkTextSurface(context, config, isSysDark)
    val primaryText = if (darkText) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val secondaryText = if (darkText) Color(0xB30F172A) else Color(0xB3F8FAFC)
    val pillColor = if (darkText) Color(0x1F0F172A) else Color(0x2BFFFFFF)
    val accent = Color(0xFF0EA5E9)

    val fontFamily = when (config.fontStyle) {
        FontStylePreset.SANS_CLEAN -> FontFamily.SansSerif
        FontStylePreset.SERIF_EDITORIAL -> FontFamily.Serif
        FontStylePreset.MONO_TECH -> FontFamily.Monospace
        FontStylePreset.ROUNDED_SOFT -> FontFamily.SansSerif
    }

    val radius = config.cornerRadiusDp.dp
    val shape = RoundedCornerShape(radius)
    val (topMult, bottomMult) = when (config.surfaceTexture) {
        GlassSurfaceTexture.CRYSTAL_FROST -> 0.92f to 0.68f
        GlassSurfaceTexture.MATTE_SATIN -> 0.86f to 0.82f
        GlassSurfaceTexture.PRISM_EDGE -> 0.94f to 0.62f
        GlassSurfaceTexture.SOFT_HALO -> 0.90f to 0.66f
    }
    val topAlpha = (config.opacity * topMult).coerceIn(0.03f, 0.96f)
    val bottomAlpha = (config.opacity * bottomMult).coerceIn(0.02f, 0.88f)

    val borderBrush = if (config.surfaceTexture == GlassSurfaceTexture.PRISM_EDGE) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF7DD3FC).copy(alpha = 0.78f),
                Color(0xFFD8B4FE).copy(alpha = 0.78f),
                Color(0xFFFDE68A).copy(alpha = 0.78f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (darkText) 0.72f else 0.34f),
                Color.White.copy(alpha = if (darkText) 0.25f else 0.12f)
            )
        )
    }

    val photoPath = when (config.templateId) {
        WidgetTemplateId.POLAROID_PHOTO -> config.photoUri
        WidgetTemplateId.GLASS_PHOTO_CAROUSEL -> config.photoCarouselUris.getOrNull(
            config.carouselIndex.coerceAtLeast(0) % config.photoCarouselUris.size.coerceAtLeast(1)
        ) ?: config.photoUri
        else -> null
    }

    val loadedBitmap = produceState<Bitmap?>(initialValue = null, photoPath) {
        value = if (photoPath != null) {
            withContext(Dispatchers.IO) {
                BitmapHelper.loadSafeWidgetBitmap(context, photoPath, maxDim = 320)
            }
        } else null
    }.value

    val abstractLayerBitmap = produceState<Bitmap?>(initialValue = null, config.templateId, darkText) {
        value = when (config.templateId) {
            WidgetTemplateId.ABSTRACT_AURA_CLOCK -> withContext(Dispatchers.Default) {
                BitmapHelper.renderAbstractAuraBitmap(darkText = darkText, widthPx = 280, heightPx = 280)
            }
            WidgetTemplateId.ABSTRACT_BREATHING_WAVE -> withContext(Dispatchers.Default) {
                BitmapHelper.renderAbstractWaveBitmap(darkText = darkText, widthPx = 420, heightPx = 210)
            }
            else -> null
        }
    }.value

    val dialBitmap = produceState<Bitmap?>(initialValue = null, config.templateId, darkText) {
        value = when (config.templateId) {
            WidgetTemplateId.ANALOG_RING_CLOCK -> withContext(Dispatchers.Default) {
                val now = LocalTime.now()
                BitmapHelper.renderAnalogClockDialBitmap(now.hour, now.minute, darkText, sizePx = 180)
            }
            WidgetTemplateId.CIRCULAR_BATTERY_RING -> withContext(Dispatchers.Default) {
                BitmapHelper.renderBatteryRingBitmap(84, true, darkText, sizePx = 180)
            }
            WidgetTemplateId.ABSTRACT_GEOMETRIC_TIME -> withContext(Dispatchers.Default) {
                val now = LocalTime.now()
                BitmapHelper.renderAbstractGeometricDialBitmap(now.hour, now.minute, darkText, sizePx = 180)
            }
            WidgetTemplateId.ABSTRACT_MOON_PHASE -> withContext(Dispatchers.Default) {
                val moon = MoonPhaseCalculator.current()
                BitmapHelper.renderMoonPhaseBitmap(moon.illuminationFraction, moon.isWaxing, darkText, sizePx = 160)
            }
            WidgetTemplateId.ABSTRACT_CONCENTRIC_PROGRESS -> withContext(Dispatchers.Default) {
                val now = LocalTime.now()
                val today = LocalDate.now()
                BitmapHelper.renderOrbitalRingsBitmap(
                    dayFraction = (now.hour * 60 + now.minute) / 1440f,
                    monthFraction = today.dayOfMonth.toFloat() / today.lengthOfMonth().toFloat(),
                    yearFraction = today.dayOfYear.toFloat() / today.lengthOfYear().toFloat(),
                    darkText = darkText,
                    sizePx = 180
                )
            }
            WidgetTemplateId.ABSTRACT_SOLAR_ARC -> withContext(Dispatchers.Default) {
                val now = LocalTime.now()
                BitmapHelper.renderSolarHorizonArcBitmap(now.hour, now.minute, darkText, widthPx = 300, heightPx = 120)
            }
            WidgetTemplateId.ABSTRACT_DOT_MATRIX_CLOCK -> withContext(Dispatchers.Default) {
                val now = LocalTime.now()
                BitmapHelper.renderDotMatrixClockBitmap(now.hour, now.minute, darkText, widthPx = 300, heightPx = 96)
            }
            WidgetTemplateId.BINARY_TECH_CLOCK -> withContext(Dispatchers.Default) {
                val now = LocalTime.now()
                BitmapHelper.renderBinaryClockMatrixBitmap(now.hour, now.minute, darkText, sizePx = 160)
            }
            WidgetTemplateId.MINIMAL_MONTH_CALENDAR -> withContext(Dispatchers.Default) {
                BitmapHelper.renderMonthCalendarGridBitmap(LocalDate.now(), darkText, widthPx = 300, heightPx = 180)
            }
            else -> null
        }
    }.value

    Box(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = Color.Black.copy(alpha = 0.28f))
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor.copy(alpha = topAlpha),
                        baseColor.copy(alpha = bottomAlpha)
                    )
                )
            )
            .border(
                BorderStroke(
                    width = if (config.surfaceTexture == GlassSurfaceTexture.PRISM_EDGE) 1.5.dp else 1.dp,
                    brush = borderBrush
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (abstractLayerBitmap != null) {
            Image(
                bitmap = abstractLayerBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compactMode) 14.dp else 18.dp),
            contentAlignment = Alignment.Center
        ) {
            when (config.templateId) {
                WidgetTemplateId.MINIMAL_PILL_CLOCK -> {
                    val now = LocalTime.now()
                    val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = primaryText,
                            fontSize = if (compactMode) 28.sp else 34.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(pillColor)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = dateStr,
                                color = primaryText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = "84%",
                                color = secondaryText,
                                fontSize = 11.sp,
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.MINIMAL_CALENDAR_TODAY -> {
                    val today = LocalDate.now()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(color = pillColor, shape = CircleShape) {
                            Text(
                                text = today.format(DateTimeFormatter.ofPattern("EEE")).uppercase(),
                                color = accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = today.dayOfMonth.toString(),
                            color = primaryText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = today.format(DateTimeFormatter.ofPattern("MMM yyyy")).uppercase(),
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.MINIMAL_GREETING_CARD -> {
                    val now = LocalTime.now()
                    val greeting = when (now.hour) {
                        in 5..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        in 17..21 -> "Good evening"
                        else -> "Good night"
                    }
                    val namePart = config.greetingName.trim().takeIf { it.isNotEmpty() }?.let { ", $it" }.orEmpty()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$greeting$namePart",
                            color = primaryText,
                            fontSize = if (compactMode) 17.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMM"))} • 84%",
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.MINIMAL_MONTH_CALENDAR -> {
                    val today = LocalDate.now()
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = today.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase(),
                                color = primaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = "TODAY ${today.dayOfMonth}",
                                color = accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Month Calendar",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }

                WidgetTemplateId.MINIMAL_FOCUS_TIMER -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${config.focusMinutes}M FOCUS",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "${config.focusSessionsCount}",
                            color = primaryText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(color = pillColor, shape = CircleShape) {
                            Text(
                                text = "+1 Session",
                                color = primaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = fontFamily,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                WidgetTemplateId.MINIMAL_WORLD_CLOCK -> {
                    val localNow = LocalTime.now()
                    val remoteZone = runCatching { ZoneId.of(config.worldClockZoneId) }
                        .getOrElse { ZoneId.of("Europe/London") }
                    val remoteNow = ZonedDateTime.now(remoteZone)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "LOCAL",
                                color = secondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = localNow.format(DateTimeFormatter.ofPattern("HH:mm")),
                                color = primaryText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = WorldClockCities.cityLabel(config.worldClockZoneId).uppercase(),
                                color = accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = remoteNow.format(DateTimeFormatter.ofPattern("HH:mm")),
                                color = primaryText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.MINIMAL_NEXT_ALARM -> {
                    val now = LocalTime.now()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NEXT ALARM",
                                color = accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Tomorrow 07:00",
                                color = primaryText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM")),
                                color = secondaryText,
                                fontSize = 11.sp,
                                fontFamily = fontFamily
                            )
                        }
                        Surface(color = pillColor, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                                color = primaryText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                WidgetTemplateId.ABSTRACT_AURA_CLOCK -> {
                    val now = LocalTime.now()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = primaryText,
                            fontSize = if (compactMode) 28.sp else 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE • d MMM")),
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.ABSTRACT_BREATHING_WAVE -> {
                    val now = LocalTime.now()
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = primaryText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = config.customTitle.ifBlank { "Breathe • Stay Present" },
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.ABSTRACT_SOLAR_ARC -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Solar Horizon Arc",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (compactMode) 60.dp else 76.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("06:00", color = secondaryText, fontSize = 10.sp, fontFamily = fontFamily)
                            Text(
                                text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                                color = primaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                            Text("18:00", color = secondaryText, fontSize = 10.sp, fontFamily = fontFamily)
                        }
                    }
                }

                WidgetTemplateId.ABSTRACT_GEOMETRIC_TIME -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Geometric dial",
                                modifier = Modifier.size(if (compactMode) 68.dp else 94.dp)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.ABSTRACT_MOON_PHASE -> {
                    val moon = MoonPhaseCalculator.current()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = moon.name,
                                modifier = Modifier.size(if (compactMode) 56.dp else 76.dp)
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = moon.name,
                            color = primaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "${(moon.illuminationFraction * 100f).roundToInt()}% lit",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.ABSTRACT_CONCENTRIC_PROGRESS -> {
                    val now = LocalTime.now()
                    val today = LocalDate.now()
                    val dayPct = ((now.hour * 60 + now.minute) / 1440f * 100f).roundToInt()
                    val monthPct = ((today.dayOfMonth.toFloat() / today.lengthOfMonth()) * 100f).roundToInt()
                    val yearPct = ((today.dayOfYear.toFloat() / today.lengthOfYear()) * 100f).roundToInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Orbital rings",
                                modifier = Modifier.size(if (compactMode) 76.dp else 96.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "DAY  $dayPct%",
                                color = primaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = "MONTH  $monthPct%",
                                color = secondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = "YEAR  $yearPct%",
                                color = secondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.ABSTRACT_DOT_MATRIX_CLOCK -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Dot Matrix Clock",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (compactMode) 56.dp else 70.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE • d MMM yyyy")).uppercase(),
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.FROSTED_DIGITAL_CLOCK -> {
                    val now = LocalTime.now()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = primaryText,
                            fontSize = if (compactMode) 30.sp else 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM")).uppercase(),
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.MINIMAL_FLIP_CLOCK -> {
                    val now = LocalTime.now()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FlipTilePreview(now.format(DateTimeFormatter.ofPattern("HH")), pillColor, primaryText, fontFamily)
                            FlipTilePreview(now.format(DateTimeFormatter.ofPattern("mm")), pillColor, primaryText, fontFamily)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.ANALOG_RING_CLOCK -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Analog clock",
                                modifier = Modifier.size(if (compactMode) 70.dp else 94.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE • d MMM")),
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.VERTICAL_TYPO_CLOCK -> {
                    val now = LocalTime.now()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("HH")),
                            color = primaryText,
                            fontSize = if (compactMode) 28.sp else 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = fontFamily,
                            lineHeight = 32.sp
                        )
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("mm")),
                            color = accent,
                            fontSize = if (compactMode) 28.sp else 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = fontFamily,
                            lineHeight = 32.sp
                        )
                    }
                }

                WidgetTemplateId.DAY_PROGRESS_BAR -> {
                    val now = LocalTime.now()
                    val pct = (((now.hour * 60 + now.minute) / 1440f) * 100f).roundToInt().coerceIn(0, 100)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = primaryText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "DAY PROGRESS • $pct%",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(10.dp))
                        PreviewProgressBar(pct / 100f, pillColor, accent)
                    }
                }

                WidgetTemplateId.GOLDEN_HOUR_DIAL -> {
                    val now = LocalTime.now()
                    val isMorning = now.hour < 11
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isMorning) "MORNING GOLDEN HOUR" else "EVENING GOLDEN HOUR",
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = if (isMorning) "06:15 – 07:05" else "17:25 – 18:15",
                            color = primaryText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "${if (isMorning) "Blue Hour • 05:50" else "Blue Hour • 18:20"}  •  Now ${now.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.BINARY_TECH_CLOCK -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        dialBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Binary Clock",
                                modifier = Modifier.size(if (compactMode) 68.dp else 86.dp)
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.CIRCULAR_BATTERY_RING -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            dialBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Battery ring",
                                    modifier = Modifier.size(if (compactMode) 68.dp else 88.dp)
                                )
                            }
                            Text(
                                text = "84%",
                                color = primaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryChargingFull,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "CHARGING",
                                color = secondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.STORAGE_RAM_PILL -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STORAGE  74 / 128 GB",
                            color = primaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        PreviewProgressBar(0.58f, pillColor, accent)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "RAM  4.6 / 8.0 GB",
                            color = primaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        PreviewProgressBar(0.57f, pillColor, Color(0xFFA78BFA))
                    }
                }

                WidgetTemplateId.THERMAL_BATTERY_CARD -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "THERMAL & POWER",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "31.5°C  •  4.12V",
                            color = primaryText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "Battery 84% • Optimal",
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.QUICK_STATUS_CARD -> {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "DEVICE STATUS",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Wifi, null, tint = accent, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Wi-Fi Connected", color = primaryText, fontSize = 12.sp, fontFamily = fontFamily)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.WbSunny, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Brightness 65% • Battery 84%", color = secondaryText, fontSize = 11.sp, fontFamily = fontFamily)
                        }
                    }
                }

                WidgetTemplateId.SYSTEM_UPTIME_PILL -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SYSTEM UPTIME",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "4d 14h 28m",
                            color = primaryText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "Pixel • API 36 • RAM 57%",
                            color = secondaryText,
                            fontSize = 11.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.EXAM_COUNTDOWN -> {
                    val daysLeft = ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        LocalDate.ofEpochDay(config.targetEpochDay)
                    ).coerceAtLeast(0L)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = config.customTitle.uppercase(),
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "$daysLeft",
                            color = primaryText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = if (daysLeft == 1L) "DAY LEFT" else "DAYS LEFT",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.HABIT_STREAK -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = config.habitName.uppercase(),
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${config.habitStreakCount}d",
                            color = primaryText,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(color = pillColor, shape = CircleShape) {
                            Text(
                                text = "+1 Today",
                                color = primaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.DAILY_WATER_TRACKER -> {
                    val goal = config.waterDailyGoal.coerceAtLeast(1)
                    val frac = (config.waterGlassesCount.toFloat() / goal).coerceIn(0f, 1f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "HYDRATION",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "${config.waterGlassesCount} / $goal",
                            color = primaryText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(5.dp))
                        PreviewProgressBar(frac, pillColor, accent)
                        Spacer(Modifier.height(5.dp))
                        Surface(color = pillColor, shape = CircleShape) {
                            Text(
                                text = "+1 Glass",
                                color = primaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.TODO_CHECKLIST -> {
                    val items = config.todoItems.ifEmpty {
                        listOf("Morning deep work block", "30m workout & hydration", "Read 15 pages before bed")
                    }.take(3)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "TODAY'S CHECKLIST",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(5.dp))
                        items.forEachIndexed { idx, task ->
                            val isDone = (config.todoCompletedMask and (1 shl idx)) != 0
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isDone) accent else secondaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    text = task,
                                    color = if (isDone) secondaryText else primaryText,
                                    fontSize = 12.sp,
                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = fontFamily
                                )
                            }
                        }
                    }
                }

                WidgetTemplateId.DAILY_STEP_GOAL -> {
                    val goal = config.stepsDailyGoal.coerceAtLeast(1000)
                    val frac = (config.stepsCount.toFloat() / goal).coerceIn(0f, 1f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STEPS",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Text(
                            text = "${config.stepsCount}",
                            color = primaryText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(5.dp))
                        PreviewProgressBar(frac, pillColor, Color(0xFF34D399))
                        Spacer(Modifier.height(5.dp))
                        Surface(color = pillColor, shape = CircleShape) {
                            Text(
                                text = "+500 Steps",
                                color = primaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.STICKY_NOTE -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "QUICK NOTE",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = config.customNoteOrQuote,
                            color = primaryText,
                            fontSize = 12.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.YEAR_PROGRESS_DOTS -> {
                    val today = LocalDate.now()
                    val pct = ((today.dayOfYear.toFloat() / today.lengthOfYear()) * 100f).roundToInt()
                    val filled = (pct / 10).coerceIn(1, 10)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${today.year} PROGRESS • $pct%",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..10) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (i <= filled) primaryText else pillColor)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Day ${today.dayOfYear} of ${today.lengthOfYear()}",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.WEEKLY_STREAK_GRID -> {
                    val todayDow = LocalDate.now().dayOfWeek.value
                    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = config.habitName.uppercase(),
                                color = secondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = fontFamily
                            )
                            Text(
                                text = "${config.habitStreakCount}d streak",
                                color = accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayLabels.forEachIndexed { idx, label ->
                                val active = (idx + 1) <= todayDow
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (active) accent else pillColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (active) Color.White else secondaryText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFamily
                                    )
                                }
                            }
                        }
                    }
                }

                WidgetTemplateId.CAFFEINE_TRACKER -> {
                    val limit = config.caffeineDailyLimit.coerceAtLeast(1)
                    val mg = config.caffeineCupsCount * 80
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CAFFEINE • ${mg}MG",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${config.caffeineCupsCount} / $limit cups",
                            color = primaryText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(5.dp))
                        Surface(color = pillColor, shape = CircleShape) {
                            Text(
                                text = "+1 Cup",
                                color = primaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                WidgetTemplateId.DAILY_BUDGET_PILL -> {
                    val limit = config.dailyBudgetLimit.coerceAtLeast(100)
                    val frac = (config.dailySpendAmount.toFloat() / limit).coerceIn(0f, 1f)
                    val barColor = if (config.dailySpendAmount > limit) Color(0xFFF97316) else accent
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DAILY SPEND",
                                    color = secondaryText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                                Text(
                                    text = "₹${config.dailySpendAmount} / ₹$limit",
                                    color = primaryText,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily
                                )
                            }
                            Surface(color = pillColor, shape = CircleShape) {
                                Text(
                                    text = "+₹100",
                                    color = primaryText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontFamily = fontFamily
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        PreviewProgressBar(frac, pillColor, barColor)
                    }
                }

                WidgetTemplateId.POLAROID_PHOTO,
                WidgetTemplateId.GLASS_PHOTO_CAROUSEL -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (compactMode) 64.dp else 96.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (loadedBitmap != null) {
                                Image(
                                    bitmap = loadedBitmap.asImageBitmap(),
                                    contentDescription = "Selected photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    tint = secondaryText,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = if (config.templateId == WidgetTemplateId.GLASS_PHOTO_CAROUSEL) {
                                "Photo Carousel • Tap to cycle"
                            } else {
                                config.customTitle.ifBlank { "Polaroid Frame" }
                            },
                            color = primaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.MINIMAL_QUOTE -> {
                    val quote = CuratedQuotes.items[config.quoteIndex % CuratedQuotes.items.size]
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "“${quote.first}”",
                            color = primaryText,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "— ${quote.second}",
                            color = secondaryText,
                            fontSize = 10.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                WidgetTemplateId.QUICK_LAUNCH_DOCK -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "QUICK LAUNCH DOCK",
                            color = secondaryText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (idx in 0 until 4) {
                                val label = DockAppPresets.shortLabelForPackage(
                                    config.favoritePackages.getOrNull(idx)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(pillColor)
                                        .padding(horizontal = 9.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = primaryText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipTilePreview(
    digits: String,
    bgColor: Color,
    textColor: Color,
    fontFamily: FontFamily
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digits,
            color = textColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily
        )
    }
}

@Composable
private fun PreviewProgressBar(
    fraction: Float,
    trackColor: Color,
    fillColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0.05f, 1f))
                .height(7.dp)
                .clip(CircleShape)
                .background(fillColor)
        )
    }
}

fun resolveComposeGlassColor(config: WidgetConfig, isSysDark: Boolean): Color {
    config.customHexColor?.let { hex ->
        BitmapHelper.parseHexOrNull(hex)?.let { return Color(it) }
    }
    if (config.themeMode == ThemeMode.DARK && config.tintPreset.prefersDarkText) {
        return Color(0xFF111827)
    }
    if (config.themeMode == ThemeMode.LIGHT && !config.tintPreset.prefersDarkText) {
        return Color(0xFFFFFFFF)
    }
    if (config.tintPreset == GlassTintPreset.MATERIAL_YOU_AUTO) {
        return if (isSysDark) Color(0xFF282E42) else Color(0xFFEDE9FE)
    }
    return Color(config.tintPreset.baseColorHex)
}
