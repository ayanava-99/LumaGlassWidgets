package com.lumaglass.widgets.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumaglass.widgets.BuildConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.monetization.BillingManager

@Composable
fun ProSettingsScreen(
    isProUnlocked: Boolean,
    proPriceLabel: String,
    rewardedUnlocked: Set<WidgetTemplateId>,
    billingStatusMessage: String?,
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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pro & Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Pro Lifetime Upgrade Hero Card
        Surface(
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0F2942), Color(0xFF1E1B4B))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isProUnlocked) "LumaGlass Pro Active" else "Unlock All Widgets & Remove Ads",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }

                    Text(
                        text = if (isProUnlocked) {
                            "Thank you for supporting LumaGlass! All 16 widgets, custom hex colors, and pro fonts are unlocked forever."
                        } else {
                            "$proPriceLabel Lifetime • One-time purchase (no subscription)"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFBAE6FD)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ProBenefitRow("All 16 Liquid Glass Widgets unlocked immediately")
                        ProBenefitRow("Zero Ads forever (no rewarded or interstitial ads)")
                        ProBenefitRow("Custom Hex Glass Colors & Pro Typography fonts")
                    }

                    if (!isProUnlocked) {
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = onPurchasePro,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Upgrade to Pro — $proPriceLabel Lifetime",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Watch Ad to Unlock Flow
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Watch Ad to Unlock ($unlockedCount / ${WidgetTemplateId.entries.size} Unlocked)",
                    style = MaterialTheme.typography.titleMedium
                )
                if (lockedWidgets.isEmpty()) {
                    Text(
                        text = "Every widget in LumaGlass v1.0 is unlocked on this device!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF86EFAC)
                    )
                } else {
                    Text(
                        text = "Prefer not to buy Pro? Watch a short test rewarded ad to permanently unlock any widget below:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    lockedWidgets.forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(template.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(template.category.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(onClick = { onWatchAdForWidget(template) }) {
                                Icon(Icons.Rounded.PlayCircleOutline, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Unlock", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Utilities & Billing Actions
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("App Utilities & Privacy", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onRestorePurchases,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Restore, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Restore Pro", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onRefreshAllWidgets,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sync Widgets", fontSize = 12.sp)
                    }
                }
                OutlinedButton(
                    onClick = { showPrivacyDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.PrivacyTip, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("100% Offline Architecture & Privacy Notice", fontSize = 12.sp)
                }

                if (BuildConfig.DEBUG) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Debug Sandbox: Simulate Pro", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Local testing toggle (${BillingManager.PRODUCT_ID_PRO_LIFETIME})",
                                fontSize = 11.sp,
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
            title = { Text("Play Billing Status") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onDismissBillingMessage) { Text("OK") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Offline Guarantee") },
            text = {
                Text(
                    "• Zero login, zero user accounts, and zero proprietary backend servers.\n" +
                        "• All widget configs, sticky notes, countdowns, and downscaled photos stay 100% on your device in DataStore & app-private storage.\n" +
                        "• Google AdMob & Google Play Billing handle ads and the optional ₹49 Pro purchase."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun ProBenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4ADE80),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, fontSize = 13.sp, color = Color(0xFFE2E8F0))
    }
}
