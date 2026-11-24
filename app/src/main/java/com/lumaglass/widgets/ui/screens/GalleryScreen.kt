package com.lumaglass.widgets.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.data.AppColorTheme
import com.lumaglass.widgets.data.CuratedStylePack
import com.lumaglass.widgets.data.CuratedWidgetPacks
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
    favoriteTemplates: Set<WidgetTemplateId>,
    selectedWallpaper: WallpaperBackdrop,
    selectedAppTheme: AppColorTheme,
    onSelectWallpaper: (WallpaperBackdrop) -> Unit,
    onSelectAppTheme: (AppColorTheme) -> Unit,
    onToggleFavorite: (WidgetTemplateId) -> Unit,
    onOpenWidgetEditor: (WidgetTemplateId) -> Unit,
    onOpenCuratedPack: (CuratedStylePack) -> Unit,
    onWatchRewardedAd: (WidgetTemplateId) -> Unit,
    onOpenProUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(WidgetCategory.ALL) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showThemeDrawer by remember { mutableStateOf(false) }
    var lockedPromptTemplate by remember { mutableStateOf<WidgetTemplateId?>(null) }

    val templates = remember(selectedCategory, showFavoritesOnly, favoriteTemplates, searchQuery) {
        val byCategory = when {
            showFavoritesOnly -> WidgetTemplateId.entries.filter { it in favoriteTemplates }
            selectedCategory == WidgetCategory.ALL -> WidgetTemplateId.entries
            else -> WidgetTemplateId.entries.filter { it.category == selectedCategory }
        }
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            byCategory
        } else {
            byCategory.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.subtitle.contains(q, ignoreCase = true) ||
                    it.category.title.contains(q, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Minimal Google-style Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
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
                        text = "${WidgetTemplateId.entries.size} Liquid Glass Widgets • ${selectedAppTheme.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { showThemeDrawer = !showThemeDrawer },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (showThemeDrawer) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Themes & Backdrops",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (isProUnlocked) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        modifier = Modifier.clickable { onOpenProUpgrade() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = if (isProUnlocked) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (isProUnlocked) "Pro" else "Get Pro",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isProUnlocked) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }

            // Expandable Theme & Backdrop Studio Bar
            AnimatedVisibility(visible = showThemeDrawer) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "App Color Theme",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppColorTheme.entries.forEach { theme ->
                                val active = theme == selectedAppTheme
                                Surface(
                                    shape = CircleShape,
                                    color = if (active) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    border = if (active) {
                                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else null,
                                    modifier = Modifier.clickable { onSelectAppTheme(theme) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(theme.primaryHex))
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = theme.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Preview Wallpaper Canvas",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WallpaperBackdrop.entries.forEach { backdrop ->
                                val active = backdrop == selectedWallpaper
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable(role = Role.Button) {
                                        onSelectWallpaper(backdrop)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (active) {
                                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                } else {
                                                    Modifier.border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                                                }
                                            )
                                            .padding(if (active) 3.dp else 0.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(backdrop.colors))
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = backdrop.label,
                                        fontSize = 10.sp,
                                        color = if (active) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Minimal Search Pill
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search 40 widgets (clock, calendar, habit, solar...)",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Clean Category Filter Chips + Favorites Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = { showFavoritesOnly = !showFavoritesOnly },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Saved (${favoriteTemplates.size})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = if (showFavoritesOnly) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                WidgetCategory.entries.forEach { category ->
                    val selected = !showFavoritesOnly && category == selectedCategory
                    val count = if (category == WidgetCategory.ALL) {
                        WidgetTemplateId.entries.size
                    } else {
                        WidgetTemplateId.entries.count { it.category == category }
                    }
                    FilterChip(
                        selected = selected,
                        onClick = {
                            showFavoritesOnly = false
                            selectedCategory = category
                        },
                        label = {
                            Text(
                                text = "${category.title} ($count)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = if (selected) {
                            null
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }

        // Gallery List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedCategory == WidgetCategory.ALL && !showFavoritesOnly && searchQuery.isBlank()) {
                item(key = "curated_packs_section") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "1-Tap Curated Setup Packs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CuratedWidgetPacks.items.forEach { pack ->
                                val packUnlocked = isProUnlocked ||
                                    pack.templateId.isFreeByDefault ||
                                    pack.templateId in rewardedUnlocked
                                val packPreview = remember(pack) { pack.toConfig("pack_${pack.id}") }
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .width(216.dp)
                                        .clickable {
                                            if (packUnlocked) {
                                                onOpenCuratedPack(pack)
                                            } else {
                                                lockedPromptTemplate = pack.templateId
                                            }
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        WallpaperPreviewCanvas(
                                            backdrop = selectedWallpaper,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(112.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .padding(10.dp)
                                        ) {
                                            GlassWidgetPreviewCard(
                                                config = packPreview,
                                                compactMode = true,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .align(Alignment.Center)
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = pack.title,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = pack.tag,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            if (!packUnlocked) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Lock,
                                                    contentDescription = "Locked",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(15.dp)
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

            items(templates, key = { it.name }) { template ->
                val isUnlocked = isProUnlocked || template.isFreeByDefault || template in rewardedUnlocked
                val isFavorite = template in favoriteTemplates
                val sampleTint = when (template.category) {
                    WidgetCategory.MINIMAL -> GlassTintPreset.FROSTED_WHITE
                    WidgetCategory.ABSTRACT -> GlassTintPreset.MIDNIGHT_COBALT
                    WidgetCategory.CLOCKS -> GlassTintPreset.FROSTED_WHITE
                    WidgetCategory.BATTERY -> GlassTintPreset.PASTEL_BLUE
                    WidgetCategory.PRODUCTIVITY -> GlassTintPreset.EMERALD_MINT
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
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
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
                    Column(modifier = Modifier.padding(10.dp)) {
                        WallpaperPreviewCanvas(
                            backdrop = selectedWallpaper,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(164.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            GlassWidgetPreviewCard(
                                config = previewConfig,
                                compactMode = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .align(Alignment.Center)
                            )

                            // Whisper-quiet size / lock badge
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.36f),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = "Pro",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (isUnlocked) template.defaultSizeLabel else "Pro • ${template.defaultSizeLabel}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.92f)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 4.dp, top = 12.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = template.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onToggleFavorite(template) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Remove favorite" else "Save favorite",
                                    tint = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            FilledTonalIconButton(
                                onClick = {
                                    if (isUnlocked) {
                                        onOpenWidgetEditor(template)
                                    } else {
                                        lockedPromptTemplate = template
                                    }
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (isUnlocked) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    }
                                )
                            ) {
                                Icon(
                                    imageVector = if (isUnlocked) {
                                        Icons.AutoMirrored.Rounded.ArrowForward
                                    } else {
                                        Icons.Rounded.Lock
                                    },
                                    contentDescription = if (isUnlocked) "Customize" else "Unlock",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    lockedPromptTemplate?.let { template ->
        AlertDialog(
            onDismissRequest = { lockedPromptTemplate = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Unlock ${template.title}") },
            text = {
                Text(
                    "Watch one short ad to unlock this widget permanently, or upgrade to LumaGlass Pro (₹49 one-time) for all ${WidgetTemplateId.entries.size} widgets, custom hex tints, and zero ads."
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
                    Text("Watch Ad")
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
                        Text("Pro ₹49")
                    }
                }
            }
        )
    }
}
