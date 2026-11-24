package com.lumaglass.widgets.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddHome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.data.BitmapHelper
import com.lumaglass.widgets.data.DockAppPresets
import com.lumaglass.widgets.data.FontStylePreset
import com.lumaglass.widgets.data.GlassSurfaceTexture
import com.lumaglass.widgets.data.GlassTintPreset
import com.lumaglass.widgets.data.ThemeMode
import com.lumaglass.widgets.data.WidgetCategory
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.data.WorldClockCities
import com.lumaglass.widgets.data.usesProStyling
import com.lumaglass.widgets.glance.AbstractWidgetReceiver
import com.lumaglass.widgets.glance.BatteryWidgetReceiver
import com.lumaglass.widgets.glance.ClockWidgetReceiver
import com.lumaglass.widgets.glance.MinimalWidgetReceiver
import com.lumaglass.widgets.glance.PhotoWidgetReceiver
import com.lumaglass.widgets.glance.ProductivityWidgetReceiver
import com.lumaglass.widgets.ui.components.GlassWidgetPreviewCard
import com.lumaglass.widgets.ui.components.ShareHelper
import com.lumaglass.widgets.ui.theme.WallpaperBackdrop
import com.lumaglass.widgets.ui.theme.WallpaperPreviewCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private enum class PreviewSizeOption(val label: String, val widthDp: Int, val heightDp: Int) {
    COMPACT("2×2", 168, 168),
    WIDE("4×2", 310, 146),
    LARGE("4×4", 290, 236)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetEditorScreen(
    initialConfig: WidgetConfig,
    isProUnlocked: Boolean,
    selectedWallpaper: WallpaperBackdrop,
    isLauncherConfigureFlow: Boolean,
    onSaveConfig: suspend (WidgetConfig) -> Unit,
    onBack: () -> Unit,
    onWatchAdForStylePass: (onGranted: () -> Unit) -> Unit,
    onPurchaseProDirect: () -> Unit,
    onAfterPinOrSave: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var config by remember(initialConfig.instanceId, initialConfig.templateId) {
        mutableStateOf(initialConfig)
    }
    var previewSize by remember {
        mutableStateOf(
            when (initialConfig.templateId.defaultSizeLabel) {
                "4×2" -> PreviewSizeOption.WIDE
                "4×4" -> PreviewSizeOption.LARGE
                else -> PreviewSizeOption.COMPACT
            }
        )
    }
    var customHexInput by remember { mutableStateOf(initialConfig.customHexColor.orEmpty()) }
    var selectedDockSlot by remember { mutableStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showManualPinDialog by remember { mutableStateOf(false) }
    var showProStyleGateDialog by remember { mutableStateOf(false) }
    var stylePassGrantedForSession by remember { mutableStateOf(false) }
    var pinFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val executeSaveAndPin: () -> Unit = {
        scope.launch {
            onSaveConfig(config)
            if (isLauncherConfigureFlow) {
                onAfterPinOrSave()
            } else {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val receiverClass = when (config.templateId.category) {
                    WidgetCategory.MINIMAL -> MinimalWidgetReceiver::class.java
                    WidgetCategory.ABSTRACT -> AbstractWidgetReceiver::class.java
                    WidgetCategory.CLOCKS, WidgetCategory.ALL -> ClockWidgetReceiver::class.java
                    WidgetCategory.BATTERY -> BatteryWidgetReceiver::class.java
                    WidgetCategory.PRODUCTIVITY -> ProductivityWidgetReceiver::class.java
                    WidgetCategory.PHOTO -> PhotoWidgetReceiver::class.java
                }
                val provider = ComponentName(context, receiverClass)
                val requested = if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    runCatching {
                        appWidgetManager.requestPinAppWidget(provider, null, null)
                    }.getOrDefault(false)
                } else {
                    false
                }
                if (requested) {
                    pinFeedbackMessage = "Saved. Confirm the system prompt to place on your home screen."
                    onAfterPinOrSave()
                } else {
                    showManualPinDialog = true
                }
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val importedPath = withContext(Dispatchers.IO) {
                    BitmapHelper.importAndDownscalePhoto(context, uri)
                }
                if (importedPath != null) {
                    config = if (config.templateId == WidgetTemplateId.GLASS_PHOTO_CAROUSEL) {
                        val updatedList = (config.photoCarouselUris + importedPath).takeLast(6)
                        config.copy(
                            photoUri = importedPath,
                            photoCarouselUris = updatedList,
                            carouselIndex = updatedList.lastIndex
                        )
                    } else {
                        config.copy(photoUri = importedPath)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(config.templateId.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            config.templateId.category.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val randomTint = GlassTintPreset.entries.random()
                            val randomTexture = if (isProUnlocked) {
                                GlassSurfaceTexture.entries.random()
                            } else {
                                config.surfaceTexture
                            }
                            val randomOpacity = (68..92).random() / 100f
                            val randomRadius = listOf(18, 22, 24, 28, 32).random()
                            customHexInput = ""
                            config = config.copy(
                                tintPreset = randomTint,
                                surfaceTexture = randomTexture,
                                customHexColor = null,
                                opacity = randomOpacity,
                                cornerRadiusDp = randomRadius
                            )
                        }
                    ) {
                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = "Shuffle Style")
                    }
                    IconButton(
                        onClick = {
                            ShareHelper.shareWidgetShowcaseCard(context, config, selectedWallpaper)
                        }
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share Setup Card")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    pinFeedbackMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            if (!isProUnlocked && config.usesProStyling() && !stylePassGrantedForSession) {
                                showProStyleGateDialog = true
                            } else {
                                executeSaveAndPin()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isLauncherConfigureFlow) Icons.Rounded.Check else Icons.Rounded.AddHome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isLauncherConfigureFlow) "Save & Apply Widget" else "Add to Home Screen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Stage
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    WallpaperPreviewCanvas(
                        backdrop = selectedWallpaper,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(248.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        GlassWidgetPreviewCard(
                            config = config,
                            compactMode = previewSize == PreviewSizeOption.COMPACT,
                            modifier = Modifier
                                .width(previewSize.widthDp.dp)
                                .height(previewSize.heightDp.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preview size",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PreviewSizeOption.entries.forEach { option ->
                                FilterChip(
                                    selected = previewSize == option,
                                    onClick = { previewSize = option },
                                    label = { Text(option.label, fontSize = 11.sp) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }

            // Glass Appearance Controls
            EditorSectionCard(title = "Glass Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Transparency", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(config.opacity * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = config.opacity,
                    onValueChange = { config = config.copy(opacity = it) },
                    valueRange = 0f..1f
                )

                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Corner radius", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${config.cornerRadiusDp}dp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = config.cornerRadiusDp.toFloat(),
                    onValueChange = { config = config.copy(cornerRadiusDp = it.roundToInt()) },
                    valueRange = 8f..36f
                )

                Spacer(Modifier.height(10.dp))
                Text("Tint", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GlassTintPreset.entries.forEach { preset ->
                        val selected = config.tintPreset == preset && config.customHexColor == null
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                config = config.copy(tintPreset = preset, customHexColor = null)
                                customHexInput = ""
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (selected) {
                                            Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        } else {
                                            Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                        }
                                    )
                                    .padding(if (selected) 3.dp else 0.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.baseColorHex))
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = preset.label,
                                fontSize = 11.sp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("Surface Finish", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassSurfaceTexture.entries.forEach { texture ->
                        val isProTexture = texture != GlassSurfaceTexture.CRYSTAL_FROST
                        FilterChip(
                            selected = config.surfaceTexture == texture,
                            onClick = { config = config.copy(surfaceTexture = texture) },
                            shape = CircleShape,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isProTexture && !isProUnlocked) {
                                        Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(texture.label, fontSize = 12.sp)
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = customHexInput,
                    onValueChange = { input ->
                        customHexInput = input
                        val parsed = BitmapHelper.parseHexOrNull(input)
                        if (parsed != null) {
                            config = config.copy(customHexColor = input.trim())
                        } else if (input.isBlank()) {
                            config = config.copy(customHexColor = null)
                        }
                    },
                    label = {
                        Text(if (isProUnlocked) "Custom Hex (#AARRGGBB)" else "Custom Hex (Pro Preview)")
                    },
                    placeholder = { Text("#B4F0FD") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = if (!isProUnlocked) {
                        { Icon(Icons.Rounded.Lock, contentDescription = "Pro feature", modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tone & Typography
            EditorSectionCard(title = "Tone & Typography") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = config.themeMode == mode,
                            onClick = { config = config.copy(themeMode = mode) },
                            label = { Text(mode.label) },
                            shape = CircleShape
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FontStylePreset.entries.forEach { font ->
                        val isProFont = font != FontStylePreset.SANS_CLEAN
                        FilterChip(
                            selected = config.fontStyle == font,
                            onClick = { config = config.copy(fontStyle = font) },
                            shape = CircleShape,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isProFont && !isProUnlocked) {
                                        Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(font.label)
                                }
                            }
                        )
                    }
                }
                if (!isProUnlocked && config.usesProStyling()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Previewing Pro styling — watch 1 short ad or unlock Pro when pinning.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Template-specific Content Controls
            when (config.templateId) {
                WidgetTemplateId.MINIMAL_GREETING_CARD -> {
                    EditorSectionCard(title = "Greeting") {
                        OutlinedTextField(
                            value = config.greetingName,
                            onValueChange = { config = config.copy(greetingName = it) },
                            label = { Text("Your Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                WidgetTemplateId.MINIMAL_FOCUS_TIMER -> {
                    EditorSectionCard(title = "Focus Sprint") {
                        Text("Sprint Duration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(15, 25, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = config.focusMinutes == mins,
                                    onClick = { config = config.copy(focusMinutes = mins) },
                                    label = { Text("${mins}m") },
                                    shape = CircleShape
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Sessions: ${config.focusSessionsCount}", modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { config = config.copy(focusSessionsCount = 0) }) { Text("Reset") }
                            FilledTonalButton(onClick = { config = config.copy(focusSessionsCount = config.focusSessionsCount + 1) }) { Text("+1") }
                        }
                    }
                }

                WidgetTemplateId.MINIMAL_WORLD_CLOCK -> {
                    EditorSectionCard(title = "Second City") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WorldClockCities.presets.forEach { (city, zoneId) ->
                                FilterChip(
                                    selected = config.worldClockZoneId == zoneId,
                                    onClick = { config = config.copy(worldClockZoneId = zoneId) },
                                    label = { Text(city) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }

                WidgetTemplateId.ABSTRACT_BREATHING_WAVE -> {
                    EditorSectionCard(title = "Horizon Caption") {
                        OutlinedTextField(
                            value = config.customTitle,
                            onValueChange = { config = config.copy(customTitle = it) },
                            label = { Text("Calm Mantra or Tagline") },
                            placeholder = { Text("Breathe • Stay Present") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                WidgetTemplateId.DAILY_WATER_TRACKER -> {
                    EditorSectionCard(title = "Hydration Goal") {
                        Text("Daily Target (glasses)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(6, 8, 10, 12).forEach { goal ->
                                FilterChip(
                                    selected = config.waterDailyGoal == goal,
                                    onClick = { config = config.copy(waterDailyGoal = goal) },
                                    label = { Text("$goal") },
                                    shape = CircleShape
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Today: ${config.waterGlassesCount} / ${config.waterDailyGoal}", modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { config = config.copy(waterGlassesCount = (config.waterGlassesCount - 1).coerceAtLeast(0)) }) { Text("-1") }
                            FilledTonalButton(onClick = { config = config.copy(waterGlassesCount = config.waterGlassesCount + 1) }) { Text("+1") }
                        }
                    }
                }

                WidgetTemplateId.EXAM_COUNTDOWN -> {
                    EditorSectionCard(title = "Countdown") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("GATE 2027", "UPSC CSE", "JEE / NEET", "Goa Trip").forEach { preset ->
                                FilterChip(
                                    selected = config.customTitle == preset,
                                    onClick = { config = config.copy(customTitle = preset) },
                                    label = { Text(preset) },
                                    shape = CircleShape
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = config.customTitle,
                            onValueChange = { config = config.copy(customTitle = it) },
                            label = { Text("Event Title") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        val formattedDate = LocalDate.ofEpochDay(config.targetEpochDay)
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Target Date: $formattedDate")
                        }
                    }
                }

                WidgetTemplateId.HABIT_STREAK,
                WidgetTemplateId.WEEKLY_STREAK_GRID -> {
                    EditorSectionCard(title = "Habit") {
                        OutlinedTextField(
                            value = config.habitName,
                            onValueChange = { config = config.copy(habitName = it) },
                            label = { Text("Habit Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Streak: ${config.habitStreakCount} days", modifier = Modifier.weight(1f))
                            OutlinedButton(
                                onClick = {
                                    config = config.copy(habitStreakCount = (config.habitStreakCount - 1).coerceAtLeast(0))
                                }
                            ) { Text("-1") }
                            FilledTonalButton(
                                onClick = {
                                    config = config.copy(habitStreakCount = config.habitStreakCount + 1)
                                }
                            ) { Text("+1") }
                        }
                    }
                }

                WidgetTemplateId.STICKY_NOTE,
                WidgetTemplateId.MINIMAL_QUOTE -> {
                    EditorSectionCard(
                        title = if (config.templateId == WidgetTemplateId.STICKY_NOTE) "Note Content" else "Custom Quote"
                    ) {
                        OutlinedTextField(
                            value = config.customNoteOrQuote,
                            onValueChange = { config = config.copy(customNoteOrQuote = it) },
                            label = { Text("Note or Quote") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                WidgetTemplateId.POLAROID_PHOTO,
                WidgetTemplateId.GLASS_PHOTO_CAROUSEL -> {
                    EditorSectionCard(title = "Photo") {
                        if (config.templateId == WidgetTemplateId.POLAROID_PHOTO) {
                            OutlinedTextField(
                                value = config.customTitle,
                                onValueChange = { config = config.copy(customTitle = it) },
                                label = { Text("Caption") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                        } else {
                            Text(
                                text = "${config.photoCarouselUris.size} photo(s) in carousel (max 6)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        FilledTonalButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (config.templateId == WidgetTemplateId.GLASS_PHOTO_CAROUSEL) {
                                    "Add Photo to Carousel"
                                } else {
                                    "Choose Photo"
                                }
                            )
                        }
                    }
                }

                WidgetTemplateId.TODO_CHECKLIST -> {
                    EditorSectionCard(title = "To-Do Items (Interactive)") {
                        val padded = List(3) { idx -> config.todoItems.getOrElse(idx) { "Task ${idx + 1}" } }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            padded.forEachIndexed { idx, value ->
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { updatedText ->
                                        val nextList = padded.toMutableList().apply { set(idx, updatedText) }
                                        config = config.copy(todoItems = nextList)
                                    },
                                    label = { Text("Task ${idx + 1}") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            OutlinedButton(
                                onClick = { config = config.copy(todoCompletedMask = 0) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Uncheck All Tasks")
                            }
                        }
                    }
                }

                WidgetTemplateId.DAILY_STEP_GOAL -> {
                    EditorSectionCard(title = "Daily Step Goal") {
                        Text("Target Steps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(6000, 8000, 10000, 12000).forEach { target ->
                                FilterChip(
                                    selected = config.stepsDailyGoal == target,
                                    onClick = { config = config.copy(stepsDailyGoal = target) },
                                    label = { Text("$target") },
                                    shape = CircleShape
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Today: ${config.stepsCount}", modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { config = config.copy(stepsCount = (config.stepsCount - 500).coerceAtLeast(0)) }) { Text("-500") }
                            FilledTonalButton(onClick = { config = config.copy(stepsCount = config.stepsCount + 500) }) { Text("+500") }
                        }
                    }
                }

                WidgetTemplateId.CAFFEINE_TRACKER -> {
                    EditorSectionCard(title = "Daily Caffeine Limit") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 3, 4, 5).forEach { limit ->
                                FilterChip(
                                    selected = config.caffeineDailyLimit == limit,
                                    onClick = { config = config.copy(caffeineDailyLimit = limit) },
                                    label = { Text("$limit cups") },
                                    shape = CircleShape
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Today: ${config.caffeineCupsCount} / ${config.caffeineDailyLimit}", modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { config = config.copy(caffeineCupsCount = (config.caffeineCupsCount - 1).coerceAtLeast(0)) }) { Text("-1") }
                            FilledTonalButton(onClick = { config = config.copy(caffeineCupsCount = config.caffeineCupsCount + 1) }) { Text("+1") }
                        }
                    }
                }

                WidgetTemplateId.DAILY_BUDGET_PILL -> {
                    EditorSectionCard(title = "Daily Spend Cap") {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(300, 500, 1000, 2000).forEach { limit ->
                                FilterChip(
                                    selected = config.dailyBudgetLimit == limit,
                                    onClick = { config = config.copy(dailyBudgetLimit = limit) },
                                    label = { Text("₹$limit") },
                                    shape = CircleShape
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Spent: ₹${config.dailySpendAmount}", modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { config = config.copy(dailySpendAmount = 0) }) { Text("Reset ₹0") }
                            FilledTonalButton(onClick = { config = config.copy(dailySpendAmount = config.dailySpendAmount + 100) }) { Text("+₹100") }
                        }
                    }
                }

                WidgetTemplateId.QUICK_LAUNCH_DOCK -> {
                    EditorSectionCard(title = "Dock Apps (4 Slots)") {
                        val currentPackages = List(4) { idx ->
                            config.favoritePackages.getOrElse(idx) { DockAppPresets.options[idx].packageName }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (slot in 0 until 4) {
                                FilterChip(
                                    selected = selectedDockSlot == slot,
                                    onClick = { selectedDockSlot = slot },
                                    label = { Text("${slot + 1}: ${DockAppPresets.shortLabelForPackage(currentPackages[slot])}") },
                                    shape = CircleShape
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Pick app for Slot ${selectedDockSlot + 1}:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DockAppPresets.options.forEach { option ->
                                FilterChip(
                                    selected = currentPackages[selectedDockSlot] == option.packageName,
                                    onClick = {
                                        val updated = currentPackages.toMutableList().apply {
                                            set(selectedDockSlot, option.packageName)
                                        }
                                        config = config.copy(favoritePackages = updated)
                                    },
                                    label = { Text(option.displayName) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }

                else -> Unit
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    if (showProStyleGateDialog) {
        AlertDialog(
            onDismissRequest = { showProStyleGateDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Save Pro Glass Styling") },
            text = {
                Text(
                    "Your widget uses Pro styling (${config.surfaceTexture.label}, ${config.fontStyle.label}, or Custom Hex). " +
                        "Watch 1 short ad to pin this style for free, or unlock Lifetime Pro (₹49) for unlimited access & zero ads."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProStyleGateDialog = false
                        onWatchAdForStylePass {
                            stylePassGrantedForSession = true
                            executeSaveAndPin()
                        }
                    }
                ) {
                    Icon(Icons.Rounded.PlayCircleOutline, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Ad & Pin")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showProStyleGateDialog = false }) { Text("Edit More") }
                    FilledTonalButton(
                        onClick = {
                            showProStyleGateDialog = false
                            onPurchaseProDirect()
                        }
                    ) {
                        Text("Pro ₹49")
                    }
                }
            }
        )
    }

    if (showDatePicker) {
        val initialMillis = LocalDate.ofEpochDay(config.targetEpochDay)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        config = config.copy(targetEpochDay = pickedDate.toEpochDay())
                    }
                    showDatePicker = false
                }) {
                    Text("Set Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showManualPinDialog) {
        AlertDialog(
            onDismissRequest = { showManualPinDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Add Widget Manually") },
            text = {
                Text(
                    "Your style is saved. If your launcher doesn't support direct pinning:\n\n" +
                        "1. Long-press an empty space on your Home Screen.\n" +
                        "2. Tap Widgets → LumaGlass Widgets.\n" +
                        "3. Drag \"LumaGlass • ${config.templateId.category.title}\" onto your screen."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showManualPinDialog = false
                    onAfterPinOrSave()
                }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
