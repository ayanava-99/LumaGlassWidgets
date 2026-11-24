package com.lumaglass.widgets.ui.screens

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.BuildConfig
import com.lumaglass.widgets.data.AppColorTheme
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.monetization.BillingManager
import com.lumaglass.widgets.ui.theme.WallpaperBackdrop

@Composable
fun ProSettingsScreen(
    isProUnlocked: Boolean,
    proPriceLabel: String,
    rewardedUnlocked: Set<WidgetTemplateId>,
    selectedAppTheme: AppColorTheme,
    selectedWallpaper: WallpaperBackdrop,
    billingStatusMessage: String?,
    onSelectAppTheme: (AppColorTheme) -> Unit,
    onSelectWallpaper: (WallpaperBackdrop) -> Unit,
    onPurchasePro: () -> Unit,
    onRestorePurchases: () -> Unit,
    onWatchAdForWidget: (WidgetTemplateId) -> Unit,
    onToggleDebugPro: (Boolean) -> Unit,
    onRefreshAllWidgets: () -> Unit,
    onDismissBillingMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val lockedWidgets = remember(isProUnlocked, rewardedUnlocked) {
        if (isProUnlocked) emptyList()
        else WidgetTemplateId.entries.filter { !it.isFreeByDefault && it !in rewardedUnlocked }
    }
    val unlockedCount = if (isProUnlocked) {
        WidgetTemplateId.entries.size
    } else {
        WidgetTemplateId.entries.count { it.isFreeByDefault || it in rewardedUnlocked }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pro & Studio Themes",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Google-style clean tonal upgrade card
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isProUnlocked) "LumaGlass Pro Active" else "One-time upgrade • $proPriceLabel",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProBenefitRow("All ${WidgetTemplateId.entries.size} Minimal, Abstract & Glass widgets")
                    ProBenefitRow("4 Glass Surface Finishes (Crystal, Satin, Prism, Halo)")
                    ProBenefitRow("10 Glass Tints, custom hex colors & 3 font families")
                    ProBenefitRow("Zero ads across the entire app")
                }

                if (!isProUnlocked) {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onPurchasePro,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Unlock Lifetime Pro — $proPriceLabel",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Studio App Theme & Preview Backdrop Section
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Studio Color Theme", style = MaterialTheme.typography.titleMedium)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppColorTheme.entries.forEach { theme ->
                        val isSelected = selectedAppTheme == theme
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onSelectAppTheme(theme) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
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
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    "Preview Canvas Backdrop",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WallpaperBackdrop.entries.forEach { backdrop ->
                        val isSelected = selectedWallpaper == backdrop
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onSelectWallpaper(backdrop) }
                                .padding(horizontal = 11.dp, vertical = 6.dp),
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
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Rewarded Unlock section
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Widget Unlocks", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$unlockedCount / ${WidgetTemplateId.entries.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (lockedWidgets.isEmpty()) {
                    Text(
                        text = "All ${WidgetTemplateId.entries.size} widgets are unlocked on this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Text(
                        text = "Watch a short ad to unlock any individual Pro widget for free:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    lockedWidgets.forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(template.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${template.category.title} • ${template.defaultSizeLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledTonalButton(
                                onClick = { onWatchAdForWidget(template) },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Rounded.PlayCircleOutline, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Unlock", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Settings List
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsActionRow(
                    icon = Icons.Rounded.Restore,
                    title = "Restore purchases",
                    subtitle = "Check Google Play for an existing Pro license",
                    onClick = onRestorePurchases
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsActionRow(
                    icon = Icons.Rounded.Refresh,
                    title = "Sync home screen widgets",
                    subtitle = "Force refresh all pinned LumaGlass widgets",
                    onClick = onRefreshAllWidgets
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsActionRow(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Offline & privacy architecture",
                    subtitle = "No login, no servers, local device storage only",
                    onClick = { showPrivacyDialog = true }
                )

                if (BuildConfig.DEBUG) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Simulate Pro (Debug)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                BillingManager.PRODUCT_ID_PRO_LIFETIME,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = isProUnlocked, onCheckedChange = onToggleDebugPro)
                    }
                }
            }
        }
    }

    billingStatusMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onDismissBillingMessage,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Play Billing") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onDismissBillingMessage) { Text("OK") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Privacy Guarantee") },
            text = {
                Text(
                    "• Zero accounts and zero proprietary servers.\n" +
                        "• Widget styles, checklists, counters, and downscaled photos stay 100% locally on your device.\n" +
                        "• Google Play Billing & AdMob handle optional Pro purchases and rewarded ads."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProBenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
