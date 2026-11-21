package com.lumaglass.widgets.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.lumaglass.widgets.data.FontStylePreset
import com.lumaglass.widgets.data.GlassTintPreset
import com.lumaglass.widgets.data.ThemeMode
import com.lumaglass.widgets.data.WidgetCategory
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.glance.BatteryWidgetReceiver
import com.lumaglass.widgets.glance.ClockWidgetReceiver
import com.lumaglass.widgets.glance.PhotoWidgetReceiver
import com.lumaglass.widgets.glance.ProductivityWidgetReceiver
import com.lumaglass.widgets.ui.components.GlassWidgetPreviewCard
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
    onOpenProUpgrade: () -> Unit,
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showManualPinDialog by remember { mutableStateOf(false) }
    var pinFeedbackMessage by remember { mutableStateOf<String?>(null) }

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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(config.templateId.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            config.templateId.category.title,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    pinFeedbackMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                onSaveConfig(config)
                                if (isLauncherConfigureFlow) {
                                    onAfterPinOrSave()
                                } else {
                                    val appWidgetManager = AppWidgetManager.getInstance(context)
                                    val receiverClass = when (config.templateId.category) {
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
                                        pinFeedbackMessage = "Saved! Confirm the system prompt to pin on your home screen."
                                        onAfterPinOrSave()
                                    } else {
                                        showManualPinDialog = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isLauncherConfigureFlow) Icons.Rounded.Check else Icons.Rounded.AddHome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isLauncherConfigureFlow) "Save & Apply Widget" else "Add to Home Screen",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Preview Canvas with Responsive Size Switcher
            Surface(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column {
                    WallpaperPreviewCanvas(
                        backdrop = selectedWallpaper,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
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
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Responsive Preview",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PreviewSizeOption.entries.forEach { option ->
                                FilterChip(
                                    selected = previewSize == option,
                                    onClick = { previewSize = option },
                                    label = { Text(option.label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Transparency Slider (0% - 100%)
            EditorSectionCard(title = "Glass Transparency (${(config.opacity * 100).roundToInt()}%)") {
                Slider(
                    value = config.opacity,
                    onValueChange = { config = config.copy(opacity = it) },
                    valueRange = 0f..1f
                )
            }

            // Corner Radius Slider
            EditorSectionCard(title = "Corner Radius (${config.cornerRadiusDp}dp)") {
                Slider(
                    value = config.cornerRadiusDp.toFloat(),
                    onValueChange = { config = config.copy(cornerRadiusDp = it.roundToInt()) },
                    valueRange = 8f..36f
                )
            }

            // Tint Color Presets + Pro Custom Hex Color
            EditorSectionCard(title = "Glass Tint Preset") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTintPreset.entries.forEach { preset ->
                        val selected = config.tintPreset == preset && config.customHexColor == null
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.clickable {
                                config = config.copy(tintPreset = preset, customHexColor = null)
                                customHexInput = ""
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.baseColorHex))
                                )
                                Spacer(Modifier.width(7.dp))
                                Text(preset.label, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customHexInput,
                        onValueChange = { input ->
                            if (!isProUnlocked) {
                                onOpenProUpgrade()
                            } else {
                                customHexInput = input
                                val parsed = BitmapHelper.parseHexOrNull(input)
                                if (parsed != null) {
                                    config = config.copy(customHexColor = input.trim())
                                } else if (input.isBlank()) {
                                    config = config.copy(customHexColor = null)
                                }
                            }
                        },
                        label = {
                            Text(if (isProUnlocked) "Custom Hex (#AARRGGBB)" else "Custom Hex (Pro ₹49)")
                        },
                        placeholder = { Text("#B4F0FD") },
                        singleLine = true,
                        trailingIcon = if (!isProUnlocked) {
                            { Icon(Icons.Rounded.Lock, contentDescription = "Pro feature", modifier = Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Theme Mode & Font Style
            EditorSectionCard(title = "Mode & Typography") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = config.themeMode == mode,
                            onClick = { config = config.copy(themeMode = mode) },
                            label = { Text(mode.label) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
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
                            onClick = {
                                if (isProFont && !isProUnlocked) {
                                    onOpenProUpgrade()
                                } else {
                                    config = config.copy(fontStyle = font)
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isProFont && !isProUnlocked) {
                                        Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(font.label)
                                }
                            }
                        )
                    }
                }
            }

            // Widget-Specific Content Editor
            when (config.templateId) {
                WidgetTemplateId.EXAM_COUNTDOWN -> {
                    EditorSectionCard(title = "Countdown Details") {
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
                                    label = { Text(preset) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = config.customTitle,
                            onValueChange = { config = config.copy(customTitle = it) },
                            label = { Text("Exam or Event Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        val formattedDate = LocalDate.ofEpochDay(config.targetEpochDay)
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Target Date: $formattedDate")
                        }
                    }
                }

                WidgetTemplateId.HABIT_STREAK -> {
                    EditorSectionCard(title = "Habit Tracker Details") {
                        OutlinedTextField(
                            value = config.habitName,
                            onValueChange = { config = config.copy(habitName = it) },
                            label = { Text("Habit Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Current Streak: ${config.habitStreakCount} days", modifier = Modifier.weight(1f))
                            OutlinedButton(
                                onClick = {
                                    config = config.copy(habitStreakCount = (config.habitStreakCount - 1).coerceAtLeast(0))
                                }
                            ) { Text("-1") }
                            Button(
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
                        title = if (config.templateId == WidgetTemplateId.STICKY_NOTE) "Sticky Note Text" else "Custom Quote (optional)"
                    ) {
                        OutlinedTextField(
                            value = config.customNoteOrQuote,
                            onValueChange = { config = config.copy(customNoteOrQuote = it) },
                            label = { Text("Note or Quote") },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                WidgetTemplateId.POLAROID_PHOTO,
                WidgetTemplateId.GLASS_PHOTO_CAROUSEL -> {
                    EditorSectionCard(title = "Photo Selection (OOM-Safe Downscaled)") {
                        if (config.templateId == WidgetTemplateId.POLAROID_PHOTO) {
                            OutlinedTextField(
                                value = config.customTitle,
                                onValueChange = { config = config.copy(customTitle = it) },
                                label = { Text("Polaroid Caption") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                        } else {
                            Text(
                                text = "${config.photoCarouselUris.size} photo(s) added to carousel (max 6)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (config.templateId == WidgetTemplateId.GLASS_PHOTO_CAROUSEL) {
                                    "Add Photo to Carousel"
                                } else {
                                    "Pick Photo from Gallery"
                                }
                            )
                        }
                    }
                }

                else -> {
                    EditorSectionCard(title = "Widget Info") {
                        Text(
                            text = "This widget reads live system state automatically with zero background polling to preserve battery.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
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
            title = { Text("Add Widget Manually") },
            text = {
                Text(
                    "Your customization is saved! Some launchers disable automatic widget pinning.\n\n" +
                        "To add it manually:\n" +
                        "1. Long-press an empty space on your Home Screen.\n" +
                        "2. Tap Widgets → LumaGlass Widgets.\n" +
                        "3. Drag \"${config.templateId.category.title}\" onto your screen—it will load your saved style automatically."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showManualPinDialog = false
                    onAfterPinOrSave()
                }) {
                    Text("Got It")
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
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}
