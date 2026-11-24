package com.lumaglass.widgets.data

import androidx.annotation.DrawableRes
import com.lumaglass.widgets.R
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos

enum class WidgetCategory(val title: String) {
    ALL("All"),
    MINIMAL("Minimal"),
    ABSTRACT("Abstract"),
    CLOCKS("Clocks"),
    BATTERY("Battery"),
    PRODUCTIVITY("Productivity"),
    PHOTO("Photo")
}

enum class AppColorTheme(
    val id: String,
    val label: String,
    val primaryHex: Long,
    val onPrimaryHex: Long,
    val primaryContainerHex: Long,
    val onPrimaryContainerHex: Long,
    val secondaryContainerHex: Long,
    val onSecondaryContainerHex: Long,
    val tertiaryHex: Long
) {
    PIXEL_BLUE(
        id = "pixel_blue",
        label = "Pixel Blue",
        primaryHex = 0xFFA8C7FA,
        onPrimaryHex = 0xFF062E6F,
        primaryContainerHex = 0xFF0842A0,
        onPrimaryContainerHex = 0xFFD3E3FD,
        secondaryContainerHex = 0xFF222A38,
        onSecondaryContainerHex = 0xFFDCE4F2,
        tertiaryHex = 0xFFC4EED0
    ),
    EMERALD_SAGE(
        id = "emerald_sage",
        label = "Emerald Sage",
        primaryHex = 0xFF86EFAC,
        onPrimaryHex = 0xFF052E16,
        primaryContainerHex = 0xFF14532D,
        onPrimaryContainerHex = 0xFFDCFCE7,
        secondaryContainerHex = 0xFF1E2D26,
        onSecondaryContainerHex = 0xFFD6ECE0,
        tertiaryHex = 0xFFA5F3FC
    ),
    AMETHYST(
        id = "amethyst",
        label = "Amethyst",
        primaryHex = 0xFFD8B4FE,
        onPrimaryHex = 0xFF3B0764,
        primaryContainerHex = 0xFF581C87,
        onPrimaryContainerHex = 0xFFF3E8FF,
        secondaryContainerHex = 0xFF2B2338,
        onSecondaryContainerHex = 0xFFE9DFF8,
        tertiaryHex = 0xFFFBCFE8
    ),
    SUNSET_GOLD(
        id = "sunset_gold",
        label = "Sunset Gold",
        primaryHex = 0xFFFCD34D,
        onPrimaryHex = 0xFF451A03,
        primaryContainerHex = 0xFF78350F,
        onPrimaryContainerHex = 0xFFFEF3C7,
        secondaryContainerHex = 0xFF312820,
        onSecondaryContainerHex = 0xFFF5E6D3,
        tertiaryHex = 0xFFFDA4AF
    ),
    MONOCHROME(
        id = "monochrome",
        label = "Monochrome",
        primaryHex = 0xFFF1F5F9,
        onPrimaryHex = 0xFF0F172A,
        primaryContainerHex = 0xFF334155,
        onPrimaryContainerHex = 0xFFF8FAFC,
        secondaryContainerHex = 0xFF262B33,
        onSecondaryContainerHex = 0xFFE2E8F0,
        tertiaryHex = 0xFFCBD5E1
    );

    companion object {
        fun fromId(id: String): AppColorTheme =
            entries.firstOrNull { it.id == id } ?: PIXEL_BLUE
    }
}

enum class GlassTintPreset(
    val label: String,
    @DrawableRes val drawableRes: Int,
    val baseColorHex: Long,
    val borderColorHex: Long,
    val prefersDarkText: Boolean
) {
    FROSTED_WHITE(
        label = "Frosted",
        drawableRes = R.drawable.bg_glass_frosted_white,
        baseColorHex = 0xFFFFFFFF,
        borderColorHex = 0x99FFFFFF,
        prefersDarkText = true
    ),
    SMOKED_DARK(
        label = "Obsidian",
        drawableRes = R.drawable.bg_glass_smoked_dark,
        baseColorHex = 0xFF111827,
        borderColorHex = 0x4DFFFFFF,
        prefersDarkText = false
    ),
    PASTEL_BLUE(
        label = "Sky",
        drawableRes = R.drawable.bg_glass_pastel_blue,
        baseColorHex = 0xFFD8F0FF,
        borderColorHex = 0x807DD3FC,
        prefersDarkText = true
    ),
    WARM_AMBER(
        label = "Amber",
        drawableRes = R.drawable.bg_glass_warm_amber,
        baseColorHex = 0xFFFEF0C7,
        borderColorHex = 0x80FCD34D,
        prefersDarkText = true
    ),
    EMERALD_MINT(
        label = "Emerald",
        drawableRes = R.drawable.bg_glass_pastel_blue,
        baseColorHex = 0xFFD1FAE5,
        borderColorHex = 0x806EE7B7,
        prefersDarkText = true
    ),
    ROSE_QUARTZ(
        label = "Rose",
        drawableRes = R.drawable.bg_glass_frosted_white,
        baseColorHex = 0xFFFFE4E6,
        borderColorHex = 0x80FDA4AF,
        prefersDarkText = true
    ),
    LAVENDER_HAZE(
        label = "Lavender",
        drawableRes = R.drawable.bg_glass_material_you,
        baseColorHex = 0xFFF3E8FF,
        borderColorHex = 0x80D8B4FE,
        prefersDarkText = true
    ),
    MIDNIGHT_COBALT(
        label = "Cobalt",
        drawableRes = R.drawable.bg_glass_smoked_dark,
        baseColorHex = 0xFF0F2942,
        borderColorHex = 0x6638BDF8,
        prefersDarkText = false
    ),
    GRAPHITE_SLATE(
        label = "Graphite",
        drawableRes = R.drawable.bg_glass_smoked_dark,
        baseColorHex = 0xFF1E293B,
        borderColorHex = 0x5594A3B8,
        prefersDarkText = false
    ),
    MATERIAL_YOU_AUTO(
        label = "Dynamic",
        drawableRes = R.drawable.bg_glass_material_you,
        baseColorHex = 0xFFEDE9FE,
        borderColorHex = 0x80C4B5FD,
        prefersDarkText = true
    )
}

enum class GlassSurfaceTexture(val label: String) {
    CRYSTAL_FROST("Crystal Frost"),
    MATTE_SATIN("Matte Satin"),
    PRISM_EDGE("Prism Edge"),
    SOFT_HALO("Soft Halo")
}

enum class ThemeMode(val label: String) {
    AUTO("Auto"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class FontStylePreset(val label: String) {
    SANS_CLEAN("Clean Sans"),
    SERIF_EDITORIAL("Editorial Serif"),
    MONO_TECH("Tech Mono"),
    ROUNDED_SOFT("Rounded Soft")
}

enum class WidgetTemplateId(
    val title: String,
    val subtitle: String,
    val category: WidgetCategory,
    val isFreeByDefault: Boolean,
    val defaultSizeLabel: String
) {
    // Minimal (7: 4 Free + 3 Pro)
    MINIMAL_PILL_CLOCK(
        title = "Minimal Pill Time",
        subtitle = "Clean Pixel-inspired time & date strip",
        category = WidgetCategory.MINIMAL,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    MINIMAL_CALENDAR_TODAY(
        title = "Today Date Tile",
        subtitle = "Crisp weekday & oversized day number",
        category = WidgetCategory.MINIMAL,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    MINIMAL_GREETING_CARD(
        title = "Ambient Greeting",
        subtitle = "Time-aware greeting & daily date line",
        category = WidgetCategory.MINIMAL,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    MINIMAL_MONTH_CALENDAR(
        title = "Month Calendar Card",
        subtitle = "Full month 7-column calendar with today badge",
        category = WidgetCategory.MINIMAL,
        isFreeByDefault = true,
        defaultSizeLabel = "4×4"
    ),
    MINIMAL_FOCUS_TIMER(
        title = "Focus Sprint Counter",
        subtitle = "Tap to log completed deep-work sessions",
        category = WidgetCategory.MINIMAL,
        isFreeByDefault = false,
        defaultSizeLabel = "2×2"
    ),
    MINIMAL_WORLD_CLOCK(
        title = "Dual World Clock",
        subtitle = "Local time alongside a global city",
        category = WidgetCategory.MINIMAL,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),
    MINIMAL_NEXT_ALARM(
        title = "Next Alarm Pill",
        subtitle = "Live upcoming system alarm & current time",
        category = WidgetCategory.MINIMAL,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),

    // Abstract (7: 3 Free + 4 Pro)
    ABSTRACT_AURA_CLOCK(
        title = "Aurora Orb Clock",
        subtitle = "Luminous soft gradient aura with crisp time",
        category = WidgetCategory.ABSTRACT,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    ABSTRACT_BREATHING_WAVE(
        title = "Liquid Wave Horizon",
        subtitle = "Flowing abstract wave bands & calm time",
        category = WidgetCategory.ABSTRACT,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    ABSTRACT_SOLAR_ARC(
        title = "Solar Horizon Arc",
        subtitle = "Sun trajectory arc across the daylight sky",
        category = WidgetCategory.ABSTRACT,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    ABSTRACT_GEOMETRIC_TIME(
        title = "Bauhaus Geometric Dial",
        subtitle = "Sculptural translucent shapes & clock hands",
        category = WidgetCategory.ABSTRACT,
        isFreeByDefault = false,
        defaultSizeLabel = "2×2"
    ),
    ABSTRACT_MOON_PHASE(
        title = "Lunar Phase Glass",
        subtitle = "Live astronomical moon phase & illumination",
        category = WidgetCategory.ABSTRACT,
        isFreeByDefault = false,
        defaultSizeLabel = "2×2"
    ),
    ABSTRACT_CONCENTRIC_PROGRESS(
        title = "Orbital Rings",
        subtitle = "Concentric arcs for day, month & year",
        category = WidgetCategory.ABSTRACT,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),
    ABSTRACT_DOT_MATRIX_CLOCK(
        title = "Dot Matrix Time",
        subtitle = "Nothing-inspired retro LED dot matrix digits",
        category = WidgetCategory.ABSTRACT,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),

    // Clocks & Date (7: 5 Free + 2 Pro)
    FROSTED_DIGITAL_CLOCK(
        title = "Frosted Digital Clock",
        subtitle = "Crisp translucent time & date card",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    MINIMAL_FLIP_CLOCK(
        title = "Minimalist Flip Clock",
        subtitle = "Split frosted hour & minute tiles",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    ANALOG_RING_CLOCK(
        title = "Analog Ring Clock",
        subtitle = "Liquid glass dial with radial hand arc",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    VERTICAL_TYPO_CLOCK(
        title = "Vertical Typo Clock",
        subtitle = "Stacked editorial digits with accent",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = false,
        defaultSizeLabel = "2×2"
    ),
    DAY_PROGRESS_BAR(
        title = "Day Progress Bar",
        subtitle = "Time & percentage of today elapsed",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    GOLDEN_HOUR_DIAL(
        title = "Golden Hour Window",
        subtitle = "Morning & evening golden photography windows",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    BINARY_TECH_CLOCK(
        title = "Binary Matrix Clock",
        subtitle = "BCD binary dot columns with decimal readout",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = false,
        defaultSizeLabel = "2×2"
    ),

    // Battery & Device (5: 3 Free + 2 Pro)
    CIRCULAR_BATTERY_RING(
        title = "Battery Ring",
        subtitle = "Live battery arc with charging state",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    STORAGE_RAM_PILL(
        title = "Storage & RAM Pill",
        subtitle = "Dual frosted capacity meters",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    THERMAL_BATTERY_CARD(
        title = "Thermal & Voltage Pill",
        subtitle = "Live battery temperature (°C), voltage & health",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    QUICK_STATUS_CARD(
        title = "Device Status Card",
        subtitle = "Live Wi-Fi, brightness & battery level",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),
    SYSTEM_UPTIME_PILL(
        title = "System Uptime & Specs",
        subtitle = "Time since boot, Android SDK & hardware model",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),

    // Productivity (10: 6 Free + 4 Pro)
    EXAM_COUNTDOWN(
        title = "Event Countdown",
        subtitle = "Days left to GATE, UPSC, JEE or Trip",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    HABIT_STREAK(
        title = "Habit Streak",
        subtitle = "Tap +1 directly on your home screen",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    DAILY_WATER_TRACKER(
        title = "Hydration Tracker",
        subtitle = "Tap +1 glass to hit your daily water goal",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    TODO_CHECKLIST(
        title = "Interactive To-Do List",
        subtitle = "Tap tasks directly on home screen to check off",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    DAILY_STEP_GOAL(
        title = "Step Goal Counter",
        subtitle = "Tap +500 steps toward your daily movement goal",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    STICKY_NOTE(
        title = "Quick Sticky Note",
        subtitle = "Minimalist frosted glass reminder",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    YEAR_PROGRESS_DOTS(
        title = "Year Dot Grid",
        subtitle = "Visual matrix of days completed this year",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),
    WEEKLY_STREAK_GRID(
        title = "Weekly Rhythm Bar",
        subtitle = "7-day habit consistency tracker",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),
    CAFFEINE_TRACKER(
        title = "Caffeine & Tea Counter",
        subtitle = "Tap +1 cup to keep daily caffeine balanced",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = false,
        defaultSizeLabel = "2×2"
    ),
    DAILY_BUDGET_PILL(
        title = "Daily Spend Tracker",
        subtitle = "Tap +₹100 to stay under your daily budget cap",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),

    // Aesthetic & Photo (4: 1 Free + 3 Pro)
    POLAROID_PHOTO(
        title = "Polaroid Frame",
        subtitle = "Downscaled memory card with caption",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    GLASS_PHOTO_CAROUSEL(
        title = "Glass Photo Carousel",
        subtitle = "Tap to cycle curated personal photos",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = false,
        defaultSizeLabel = "4×4"
    ),
    MINIMAL_QUOTE(
        title = "Daily Quote Card",
        subtitle = "Tap to cycle mindful daily affirmations",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),
    QUICK_LAUNCH_DOCK(
        title = "Quick Launch Dock",
        subtitle = "4 favorite apps inside a frosted pill",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    );

    companion object {
        fun defaultForCategory(category: WidgetCategory): WidgetTemplateId = when (category) {
            WidgetCategory.MINIMAL -> MINIMAL_PILL_CLOCK
            WidgetCategory.ABSTRACT -> ABSTRACT_AURA_CLOCK
            WidgetCategory.CLOCKS, WidgetCategory.ALL -> FROSTED_DIGITAL_CLOCK
            WidgetCategory.BATTERY -> CIRCULAR_BATTERY_RING
            WidgetCategory.PRODUCTIVITY -> EXAM_COUNTDOWN
            WidgetCategory.PHOTO -> POLAROID_PHOTO
        }
    }
}

@Serializable
data class WidgetConfig(
    val instanceId: String,
    val appWidgetId: Int = -1,
    val templateId: WidgetTemplateId = WidgetTemplateId.MINIMAL_PILL_CLOCK,
    val opacity: Float = 0.78f,
    val cornerRadiusDp: Int = 24,
    val tintPreset: GlassTintPreset = GlassTintPreset.FROSTED_WHITE,
    val surfaceTexture: GlassSurfaceTexture = GlassSurfaceTexture.CRYSTAL_FROST,
    val customHexColor: String? = null,
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val fontStyle: FontStylePreset = FontStylePreset.SANS_CLEAN,
    val customTitle: String = "GATE 2027",
    val targetEpochDay: Long = LocalDate.now().plusDays(90).toEpochDay(),
    val customNoteOrQuote: String = "Finish 2 mock papers + revise OS & DBMS before 9 PM.",
    val habitName: String = "Deep Work 2h",
    val habitStreakCount: Int = 7,
    val quoteIndex: Int = 0,
    val photoUri: String? = null,
    val photoCarouselUris: List<String> = emptyList(),
    val carouselIndex: Int = 0,
    val favoritePackages: List<String> = listOf(
        "com.android.dialer",
        "com.google.android.apps.messaging",
        "com.android.chrome",
        "com.android.camera2"
    ),
    val worldClockZoneId: String = "Europe/London",
    val waterGlassesCount: Int = 5,
    val waterDailyGoal: Int = 8,
    val focusSessionsCount: Int = 3,
    val focusMinutes: Int = 25,
    val greetingName: String = "Alex",
    val todoItems: List<String> = listOf(
        "Morning deep work block",
        "30m workout & hydration",
        "Read 15 pages before bed"
    ),
    val todoCompletedMask: Int = 1,
    val caffeineCupsCount: Int = 2,
    val caffeineDailyLimit: Int = 4,
    val dailySpendAmount: Int = 300,
    val dailyBudgetLimit: Int = 600,
    val stepsCount: Int = 4500,
    val stepsDailyGoal: Int = 8000,
    val lastUpdatedEpochMillis: Long = System.currentTimeMillis()
)

object CuratedQuotes {
    val items: List<Pair<String, String>> = listOf(
        "Small daily discipline beats occasional intensity." to "LumaGlass",
        "Simplicity is about subtracting the obvious and adding the meaningful." to "John Maeda",
        "You have a right to perform your duty, not to the fruits of action." to "Bhagavad Gita",
        "Waste no more time arguing what a good person should be. Be one." to "Marcus Aurelius",
        "Focus on the next right step; clarity follows motion." to "Kanso",
        "Silence is an empty space; space is the home of the awakened mind." to "Buddha"
    )
}

object WorldClockCities {
    val presets: List<Pair<String, String>> = listOf(
        "London" to "Europe/London",
        "New York" to "America/New_York",
        "Tokyo" to "Asia/Tokyo",
        "Dubai" to "Asia/Dubai",
        "Singapore" to "Asia/Singapore",
        "San Francisco" to "America/Los_Angeles",
        "Sydney" to "Australia/Sydney"
    )

    fun cityLabel(zoneId: String): String =
        presets.firstOrNull { it.second == zoneId }?.first
            ?: zoneId.substringAfterLast('/').replace('_', ' ')
}

data class MoonPhaseInfo(
    val name: String,
    val illuminationFraction: Float,
    val isWaxing: Boolean
)

object MoonPhaseCalculator {
    private const val SYNODIC_MONTH = 29.53058867
    private val REFERENCE_NEW_MOON_EPOCH_SEC =
        ZonedDateTime.of(2000, 1, 6, 18, 14, 0, 0, ZoneOffset.UTC).toEpochSecond()

    fun current(): MoonPhaseInfo {
        val nowSec = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()
        val daysSinceRef = (nowSec - REFERENCE_NEW_MOON_EPOCH_SEC) / 86400.0
        val cyclePos = ((daysSinceRef % SYNODIC_MONTH) + SYNODIC_MONTH) % SYNODIC_MONTH
        val phaseFraction = (cyclePos / SYNODIC_MONTH).toFloat() // 0..1
        val illumination = ((1.0 - cos(2.0 * PI * phaseFraction)) / 2.0).toFloat().coerceIn(0f, 1f)
        val isWaxing = phaseFraction < 0.5f

        val name = when {
            phaseFraction < 0.04f || phaseFraction >= 0.96f -> "New Moon"
            phaseFraction < 0.21f -> "Waxing Crescent"
            phaseFraction < 0.29f -> "First Quarter"
            phaseFraction < 0.46f -> "Waxing Gibbous"
            phaseFraction < 0.54f -> "Full Moon"
            phaseFraction < 0.71f -> "Waning Gibbous"
            phaseFraction < 0.79f -> "Last Quarter"
            else -> "Waning Crescent"
        }
        return MoonPhaseInfo(
            name = name,
            illuminationFraction = illumination,
            isWaxing = isWaxing
        )
    }
}

fun WidgetConfig.usesProStyling(): Boolean =
    surfaceTexture != GlassSurfaceTexture.CRYSTAL_FROST ||
        fontStyle != FontStylePreset.SANS_CLEAN ||
        !customHexColor.isNullOrBlank()

data class DockAppOption(
    val shortLabel: String,
    val displayName: String,
    val packageName: String
)

object DockAppPresets {
    val options: List<DockAppOption> = listOf(
        DockAppOption("Phone", "Phone", "com.android.dialer"),
        DockAppOption("Msgs", "Messages", "com.google.android.apps.messaging"),
        DockAppOption("WA", "WhatsApp", "com.whatsapp"),
        DockAppOption("IG", "Instagram", "com.instagram.android"),
        DockAppOption("YT", "YouTube", "com.google.android.youtube"),
        DockAppOption("Spot", "Spotify", "com.spotify.music"),
        DockAppOption("Web", "Chrome", "com.android.chrome"),
        DockAppOption("Cam", "Camera", "com.android.camera2"),
        DockAppOption("Maps", "Maps", "com.google.android.apps.maps"),
        DockAppOption("Pay", "GPay", "com.google.android.apps.nbu.paisa.user"),
        DockAppOption("Mail", "Gmail", "com.google.android.gm"),
        DockAppOption("Calc", "Calculator", "com.google.android.calculator")
    )

    fun shortLabelForPackage(pkg: String?): String =
        options.firstOrNull { it.packageName == pkg }?.shortLabel
            ?: pkg?.substringAfterLast('.')?.take(5)?.replaceFirstChar { it.uppercase() }
            ?: "App"
}

data class CuratedStylePack(
    val id: String,
    val title: String,
    val tag: String,
    val templateId: WidgetTemplateId,
    val tintPreset: GlassTintPreset,
    val surfaceTexture: GlassSurfaceTexture,
    val fontStyle: FontStylePreset,
    val opacity: Float,
    val cornerRadiusDp: Int
) {
    fun toConfig(instanceId: String): WidgetConfig = WidgetConfig(
        instanceId = instanceId,
        templateId = templateId,
        tintPreset = tintPreset,
        surfaceTexture = surfaceTexture,
        fontStyle = fontStyle,
        opacity = opacity,
        cornerRadiusDp = cornerRadiusDp
    )
}

object CuratedWidgetPacks {
    val items: List<CuratedStylePack> = listOf(
        CuratedStylePack(
            id = "nothing_dot",
            title = "Nothing Dot OS",
            tag = "Retro LED",
            templateId = WidgetTemplateId.ABSTRACT_DOT_MATRIX_CLOCK,
            tintPreset = GlassTintPreset.SMOKED_DARK,
            surfaceTexture = GlassSurfaceTexture.PRISM_EDGE,
            fontStyle = FontStylePreset.MONO_TECH,
            opacity = 0.86f,
            cornerRadiusDp = 24
        ),
        CuratedStylePack(
            id = "upsc_desk",
            title = "UPSC / GATE Desk",
            tag = "Focus",
            templateId = WidgetTemplateId.EXAM_COUNTDOWN,
            tintPreset = GlassTintPreset.EMERALD_MINT,
            surfaceTexture = GlassSurfaceTexture.MATTE_SATIN,
            fontStyle = FontStylePreset.SANS_CLEAN,
            opacity = 0.82f,
            cornerRadiusDp = 24
        ),
        CuratedStylePack(
            id = "pixel_calm",
            title = "Pixel Calm",
            tag = "Minimal",
            templateId = WidgetTemplateId.MINIMAL_PILL_CLOCK,
            tintPreset = GlassTintPreset.FROSTED_WHITE,
            surfaceTexture = GlassSurfaceTexture.CRYSTAL_FROST,
            fontStyle = FontStylePreset.SANS_CLEAN,
            opacity = 0.78f,
            cornerRadiusDp = 28
        ),
        CuratedStylePack(
            id = "solar_horizon",
            title = "Solar Horizon",
            tag = "Aesthetic",
            templateId = WidgetTemplateId.ABSTRACT_SOLAR_ARC,
            tintPreset = GlassTintPreset.MIDNIGHT_COBALT,
            surfaceTexture = GlassSurfaceTexture.SOFT_HALO,
            fontStyle = FontStylePreset.SERIF_EDITORIAL,
            opacity = 0.84f,
            cornerRadiusDp = 26
        )
    )
}
