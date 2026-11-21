package com.lumaglass.widgets.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.data.GlassTintPreset
import com.lumaglass.widgets.data.WidgetCategory
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.ui.components.GlassWidgetPreviewCard
import com.lumaglass.widgets.ui.theme.WallpaperBackdrop
import com.lumaglass.widgets.ui.theme.WallpaperPreviewCanvas

@Composable
fun GalleryScreen(
    isProUnlocked: Boolean,
    rewardedUnlocked: Set<WidgetTemplateId>,
    selectedWallpaper: WallpaperBackdrop,
    onSelectWallpaper: (WallpaperBackdrop) -> Unit,
    onOpenWidgetEditor: (WidgetTemplateId) -> Unit,
    onWatchRewardedAd: (WidgetTemplateId) -> Unit,
    onOpenProUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(WidgetCategory.ALL) }
    var lockedPromptTemplate by remember { mutableStateOf<WidgetTemplateId?>(null) }

    val templates = remember(selectedCategory) {
        if (selectedCategory == WidgetCategory.ALL) {
            WidgetTemplateId.entries
        } else {
            WidgetTemplateId.entries.filter { it.category == selectedCategory }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header + Wallpaper backdrop switcher
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LumaGlass",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "16 Liquid Glass Widgets • 100% Offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = if (isProUnlocked) Color(0xFF0EA5E9).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                    modifier = Modifier.clickable { onOpenProUpgrade() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = if (isProUnlocked) Color(0xFF7DD3FC) else Color(0xFFFDE68A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isProUnlocked) "PRO" else "₹49 PRO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Wallpaper canvas swatches so users test glass widgets over different backdrops
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Canvas:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WallpaperBackdrop.entries.forEach { backdrop ->
                    val active = backdrop == selectedWallpaper
                    Surface(
                        shape = CircleShape,
                        color = if (active) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                        border = BorderStroke(
                            1.dp,
                            if (active) Color(0xFF7DD3FC) else Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.clickable { onSelectWallpaper(backdrop) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(backdrop.colors))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = backdrop.label,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Category Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WidgetCategory.entries.forEach { category ->
                    val selected = category == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        label = {
                            val count = if (category == WidgetCategory.ALL) {
                                WidgetTemplateId.entries.size
                            } else {
                                WidgetTemplateId.entries.count { it.category == category }
                            }
                            Text("${category.title} ($count)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Widget Gallery Cards on live gradient wallpaper preview canvas
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(templates, key = { it.name }) { template ->
                val isUnlocked = isProUnlocked || template.isFreeByDefault || template in rewardedUnlocked
                val sampleTint = when (template.category) {
                    WidgetCategory.CLOCKS -> GlassTintPreset.FROSTED_WHITE
                    WidgetCategory.BATTERY -> GlassTintPreset.PASTEL_BLUE
                    WidgetCategory.PRODUCTIVITY -> GlassTintPreset.WARM_AMBER
                    WidgetCategory.PHOTO -> GlassTintPreset.SMOKED_DARK
                    WidgetCategory.ALL -> GlassTintPreset.FROSTED_WHITE
                }
                val previewConfig = remember(template) {
                    WidgetConfig(
                        instanceId = "preview_${template.name}",
                        templateId = template,
                        tintPreset = sampleTint
                    )
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isUnlocked) {
                                onOpenWidgetEditor(template)
                            } else {
                                lockedPromptTemplate = template
                            }
                        }
                ) {
                    Column {
                        WallpaperPreviewCanvas(
                            backdrop = selectedWallpaper,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(166.dp)
                                .padding(18.dp)
                        ) {
                            GlassWidgetPreviewCard(
                                config = previewConfig,
                                compactMode = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(128.dp)
                                    .align(Alignment.Center)
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.45f),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = "Locked",
                                            tint = Color(0xFFFDE68A),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = when {
                                            template.isFreeByDefault -> "FREE • ${template.defaultSizeLabel}"
                                            isUnlocked -> "UNLOCKED • ${template.defaultSizeLabel}"
                                            else -> "AD / PRO • ${template.defaultSizeLabel}"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = template.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            if (isUnlocked) {
                                Button(
                                    onClick = { onOpenWidgetEditor(template) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Rounded.Tune, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Customize", fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { lockedPromptTemplate = template },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Rounded.PlayCircleOutline, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Unlock", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Unlock Modal for Locked Rewarded / Pro Widgets
    lockedPromptTemplate?.let { template ->
        AlertDialog(
            onDismissRequest = { lockedPromptTemplate = null },
            title = { Text("Unlock ${template.title}") },
            text = {
                Text(
                    "Watch one short rewarded ad to unlock this widget permanently on your device, or upgrade to LumaGlass Pro (₹49 one-time) to unlock all 16 widgets, remove ads, and unlock custom hex colors."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        lockedPromptTemplate = null
                        onWatchRewardedAd(template)
                    }
                ) {
                    Icon(Icons.Rounded.PlayCircleOutline, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Ad to Unlock")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { lockedPromptTemplate = null }) {
                        Text("Cancel")
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        onClick = {
                            lockedPromptTemplate = null
                            onOpenProUpgrade()
                        }
                    ) {
                        Text("Go Pro ₹49")
                    }
                }
            }
        )
    }
}
