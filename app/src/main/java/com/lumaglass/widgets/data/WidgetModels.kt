package com.lumaglass.widgets.data

import androidx.annotation.DrawableRes
import com.lumaglass.widgets.R
import kotlinx.serialization.Serializable
import java.time.LocalDate

enum class WidgetCategory(val title: String) {
    ALL("All"),
    CLOCKS("Clocks"),
    BATTERY("Battery"),
    PRODUCTIVITY("Productivity"),
    PHOTO("Photo")
}

enum class GlassTintPreset(
    val label: String,
    @DrawableRes val drawableRes: Int,
    val baseColorHex: Long,
    val borderColorHex: Long,
    val prefersDarkText: Boolean
) {
    FROSTED_WHITE(
        label = "Frosted White",
        drawableRes = R.drawable.bg_glass_frosted_white,
        baseColorHex = 0xFFFFFFFF,
        borderColorHex = 0x99FFFFFF,
        prefersDarkText = true
    ),
    SMOKED_DARK(
        label = "Smoked Dark",
        drawableRes = R.drawable.bg_glass_smoked_dark,
        baseColorHex = 0xFF111827,
        borderColorHex = 0x4DFFFFFF,
        prefersDarkText = false
    ),
    PASTEL_BLUE(
        label = "Pastel Blue",
        drawableRes = R.drawable.bg_glass_pastel_blue,
        baseColorHex = 0xFFD8F0FF,
        borderColorHex = 0x807DD3FC,
        prefersDarkText = true
    ),
    WARM_AMBER(
        label = "Warm Amber",
        drawableRes = R.drawable.bg_glass_warm_amber,
        baseColorHex = 0xFFFEF0C7,
        borderColorHex = 0x80FCD34D,
        prefersDarkText = true
    ),
    MATERIAL_YOU_AUTO(
        label = "Material You Auto",
        drawableRes = R.drawable.bg_glass_material_you,
        baseColorHex = 0xFFEDE9FE,
        borderColorHex = 0x80C4B5FD,
        prefersDarkText = true
    )
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
    // Clocks & Date (5: 4 Free + 1 Locked)
    FROSTED_DIGITAL_CLOCK(
        title = "Frosted Glass Digital Clock",
        subtitle = "Crisp translucent time & date card",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    MINIMAL_FLIP_CLOCK(
        title = "Minimalist Flip Clock",
        subtitle = "Retro split frosted hour & minute tiles",
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
        title = "Vertical Typography Clock",
        subtitle = "Stacked editorial digits with accent bar",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = false,
        defaultSizeLabel = "2×2"
    ),
    DAY_PROGRESS_BAR(
        title = "Day-Progress Bar",
        subtitle = "Time + percentage of today elapsed",
        category = WidgetCategory.CLOCKS,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),

    // Battery & Device (3: 2 Free + 1 Locked)
    CIRCULAR_BATTERY_RING(
        title = "Circular Glass Battery Ring",
        subtitle = "Live battery arc with charging state",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    STORAGE_RAM_PILL(
        title = "Storage + RAM Pill",
        subtitle = "Dual frosted capacity meters",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = true,
        defaultSizeLabel = "4×2"
    ),
    QUICK_STATUS_CARD(
        title = "Quick-Glance Wi-Fi & Brightness",
        subtitle = "Live connectivity & screen level card",
        category = WidgetCategory.BATTERY,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),

    // Productivity & India-Focused (4: 3 Free + 1 Locked)
    EXAM_COUNTDOWN(
        title = "Exam / Event Countdown",
        subtitle = "Days left to GATE, UPSC, JEE or Trip",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    HABIT_STREAK(
        title = "Daily Habit Streak Counter",
        subtitle = "Tap +1 directly on home screen",
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
        title = "Year Progress Dot Grid",
        subtitle = "Visual matrix of days completed this year",
        category = WidgetCategory.PRODUCTIVITY,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),

    // Aesthetic & Photo (4: 1 Free + 3 Locked)
    POLAROID_PHOTO(
        title = "Polaroid Photo Frame",
        subtitle = "Downscaled memory card with caption",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = true,
        defaultSizeLabel = "2×2"
    ),
    GLASS_PHOTO_CAROUSEL(
        title = "Rounded Glass Photo Carousel",
        subtitle = "Tap to cycle curated personal photos",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = false,
        defaultSizeLabel = "4×4"
    ),
    MINIMAL_QUOTE(
        title = "Daily Minimalist Quote Card",
        subtitle = "Tap to cycle mindful daily affirmations",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    ),
    QUICK_LAUNCH_DOCK(
        title = "Quick-Launch App Dock",
        subtitle = "4 favorite apps inside a frosted pill",
        category = WidgetCategory.PHOTO,
        isFreeByDefault = false,
        defaultSizeLabel = "4×2"
    );

    companion object {
        fun defaultForCategory(category: WidgetCategory): WidgetTemplateId = when (category) {
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
    val templateId: WidgetTemplateId = WidgetTemplateId.FROSTED_DIGITAL_CLOCK,
    val opacity: Float = 0.78f,
    val cornerRadiusDp: Int = 24,
    val tintPreset: GlassTintPreset = GlassTintPreset.FROSTED_WHITE,
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
