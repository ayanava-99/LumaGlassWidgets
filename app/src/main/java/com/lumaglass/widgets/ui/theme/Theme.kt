package com.lumaglass.widgets.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class WallpaperBackdrop(
    val id: String,
    val label: String,
    val colors: List<Color>
) {
    AURORA(
        id = "aurora",
        label = "Aurora Dark",
        colors = listOf(Color(0xFF090D16), Color(0xFF132A4A), Color(0xFF311B4E))
    ),
    SUNSET_MESH(
        id = "sunset",
        label = "Warm Sunset",
        colors = listOf(Color(0xFF1E132B), Color(0xFF6B2D5C), Color(0xFFD97757))
    ),
    HIMALAYAN_MIST(
        id = "mist",
        label = "Himalayan Mist",
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    ),
    PASTEL_BLOOM(
        id = "pastel",
        label = "Pastel Bloom",
        colors = listOf(Color(0xFF2B2D42), Color(0xFF4A4E69), Color(0xFF9A8C98))
    );

    companion object {
        fun fromId(id: String): WallpaperBackdrop =
            entries.firstOrNull { it.id == id } ?: AURORA
    }
}

private val LumaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFFC4B5FD),
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = Color(0xFF3B0764),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = Color(0xFFFDE68A),
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

private val LumaTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    )
)

@Composable
fun LumaGlassTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LumaDarkColorScheme,
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
