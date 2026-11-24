package com.lumaglass.widgets.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.data.AppColorTheme

enum class WallpaperBackdrop(
    val id: String,
    val label: String,
    val colors: List<Color>
) {
    AURORA(
        id = "aurora",
        label = "Cobalt",
        colors = listOf(Color(0xFF0B101B), Color(0xFF152C4E), Color(0xFF28204B))
    ),
    SUNSET_MESH(
        id = "sunset",
        label = "Dusk",
        colors = listOf(Color(0xFF1E142B), Color(0xFF5A2B52), Color(0xFFC26D53))
    ),
    HIMALAYAN_MIST(
        id = "mist",
        label = "Sage",
        colors = listOf(Color(0xFF0E1C21), Color(0xFF1D353C), Color(0xFF2A4D56))
    ),
    PASTEL_BLOOM(
        id = "pastel",
        label = "Slate",
        colors = listOf(Color(0xFF232635), Color(0xFF3E435A), Color(0xFF6B6578))
    ),
    OBSIDIAN_NOIR(
        id = "noir",
        label = "Noir",
        colors = listOf(Color(0xFF06070A), Color(0xFF11151C), Color(0xFF1B2230))
    ),
    FOREST_MOSS(
        id = "forest",
        label = "Forest",
        colors = listOf(Color(0xFF0A1914), Color(0xFF143328), Color(0xFF1F4B3B))
    ),
    ROSE_VELVET(
        id = "rose",
        label = "Velvet",
        colors = listOf(Color(0xFF21101C), Color(0xFF4C1D3D), Color(0xFF833857))
    ),
    NORDIC_ICE(
        id = "nordic",
        label = "Nordic",
        colors = listOf(Color(0xFF111D2C), Color(0xFF213C58), Color(0xFF3A648C))
    );

    companion object {
        fun fromId(id: String): WallpaperBackdrop =
            entries.firstOrNull { it.id == id } ?: AURORA
    }
}

private fun buildColorScheme(appTheme: AppColorTheme): ColorScheme = darkColorScheme(
    primary = Color(appTheme.primaryHex),
    onPrimary = Color(appTheme.onPrimaryHex),
    primaryContainer = Color(appTheme.primaryContainerHex),
    onPrimaryContainer = Color(appTheme.onPrimaryContainerHex),
    secondary = Color(appTheme.primaryHex),
    onSecondary = Color(appTheme.onPrimaryHex),
    secondaryContainer = Color(appTheme.secondaryContainerHex),
    onSecondaryContainer = Color(appTheme.onSecondaryContainerHex),
    tertiary = Color(appTheme.tertiaryHex),
    onTertiary = Color(0xFF072711),
    background = Color(0xFF0E1116),
    onBackground = Color(0xFFE3E6EC),
    surface = Color(0xFF161A22),
    onSurface = Color(0xFFE3E6EC),
    surfaceVariant = Color(0xFF222834),
    onSurfaceVariant = Color(0xFF9AA3B2),
    surfaceContainerLowest = Color(0xFF0A0D12),
    surfaceContainerLow = Color(0xFF13171F),
    surfaceContainer = Color(0xFF181D26),
    surfaceContainerHigh = Color(0xFF1F2530),
    surfaceContainerHighest = Color(0xFF272E3B),
    outline = Color(0xFF363E4D),
    outlineVariant = Color(0xFF252B36)
)

private val LumaTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp
    )
)

@Composable
fun LumaGlassTheme(
    appTheme: AppColorTheme = AppColorTheme.PIXEL_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = remember(appTheme) { buildColorScheme(appTheme) }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LumaTypography,
        content = content
    )
}

@Composable
fun WallpaperPreviewCanvas(
    backdrop: WallpaperBackdrop,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = backdrop.colors,
                start = Offset.Zero,
                end = Offset(1000f, 1200f)
            )
        ),
        content = content
    )
}
