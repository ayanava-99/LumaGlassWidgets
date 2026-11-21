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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Public
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.data.BitmapHelper
import com.lumaglass.widgets.data.CuratedQuotes
import com.lumaglass.widgets.data.FontStylePreset
import com.lumaglass.widgets.data.GlassTintPreset
import com.lumaglass.widgets.data.ThemeMode
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Renders an interactive, high-fidelity Compose preview of any of the 16 LumaGlass widgets.
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
    val topAlpha = (config.opacity * 0.92f).coerceIn(0.03f, 0.96f)
    val bottomAlpha = (config.opacity * 0.68f).coerceIn(0.02f, 0.85f)

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

    val dialBitmap = produceState<Bitmap?>(initialValue = null, config.templateId, darkText) {
        value = when (config.templateId) {
            WidgetTemplateId.ANALOG_RING_CLOCK -> withContext(Dispatchers.Default) {
                val now = LocalTime.now()
                BitmapHelper.renderAnalogClockDialBitmap(now.hour, now.minute, darkText, sizePx = 180)
            }
            WidgetTemplateId.CIRCULAR_BATTERY_RING -> withContext(Dispatchers.Default) {
                BitmapHelper.renderBatteryRingBitmap(84, true, darkText, sizePx = 180)
            }
            else -> null
        }
    }.value

    Box(
        modifier = modifier
            .shadow(12.dp, shape, ambientColor = Color.Black.copy(alpha = 0.3f))
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
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (darkText) 0.72f else 0.36f),
                            Color.White.copy(alpha = if (darkText) 0.25f else 0.12f)
                        )
                    )
                ),
                shape = shape
            )
            .padding(if (compactMode) 14.dp else 18.dp),
        contentAlignment = Alignment.Center
    ) {
        when (config.templateId) {
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
                        text = config.habitName,
                        color = secondaryText,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = fontFamily
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "🔥 ${config.habitStreakCount}",
                        color = primaryText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = pillColor,
                        shape = CircleShape
                    ) {
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
                        listOf(
                            Icons.Rounded.Call,
                            Icons.Rounded.ChatBubbleOutline,
                            Icons.Rounded.Public,
                            Icons.Rounded.CameraAlt
                        ).forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(pillColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = primaryText, modifier = Modifier.size(18.dp))
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
    if (config.themeMode == ThemeMode.DARK && config.tintPreset == GlassTintPreset.FROSTED_WHITE) {
        return Color(0xFF111827)
    }
    if (config.themeMode == ThemeMode.LIGHT && config.tintPreset == GlassTintPreset.SMOKED_DARK) {
        return Color(0xFFFFFFFF)
    }
    if (config.tintPreset == GlassTintPreset.MATERIAL_YOU_AUTO) {
        return if (isSysDark) Color(0xFF282E42) else Color(0xFFEDE9FE)
    }
    return Color(config.tintPreset.baseColorHex)
}
