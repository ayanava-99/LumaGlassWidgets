package com.lumaglass.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.glance.refreshAllCategoryWidgets
import com.lumaglass.widgets.ui.screens.GalleryScreen
import com.lumaglass.widgets.ui.screens.MyWidgetsScreen
import com.lumaglass.widgets.ui.screens.ProSettingsScreen
import com.lumaglass.widgets.ui.screens.WidgetEditorScreen
import com.lumaglass.widgets.ui.theme.LumaGlassTheme
import com.lumaglass.widgets.ui.theme.WallpaperBackdrop
import kotlinx.coroutines.launch
import java.util.UUID

private enum class MainTab(val label: String) {
    GALLERY("Gallery"),
    MY_WIDGETS("My Widgets"),
    PRO("Pro")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LumaGlassApp
        val launchAppWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val isLauncherConfigure = intent?.action == AppWidgetManager.ACTION_APPWIDGET_CONFIGURE &&
            launchAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
        val launchTemplateExtra = intent?.getStringExtra("template_id")?.let { raw ->
            runCatching { WidgetTemplateId.valueOf(raw) }.getOrNull()
        }

        if (isLauncherConfigure) {
            setResult(Activity.RESULT_CANCELED)
        }

        setContent {
            LumaGlassTheme {
                LumaGlassRootScreen(
                    activity = this,
                    app = app,
                    launchAppWidgetId = launchAppWidgetId,
                    isLauncherConfigure = isLauncherConfigure,
                    launchTemplateExtra = launchTemplateExtra
                )
            }
        }
    }
}

@Composable
private fun LumaGlassRootScreen(
    activity: ComponentActivity,
    app: LumaGlassApp,
    launchAppWidgetId: Int,
    isLauncherConfigure: Boolean,
    launchTemplateExtra: WidgetTemplateId?
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isProUnlocked by app.repository.isProUnlocked.collectAsStateWithLifecycle(initialValue = false)
    val rewardedUnlocked by app.repository.rewardedUnlockedTemplates.collectAsStateWithLifecycle(initialValue = emptySet())
    val configuredWidgets by app.repository.configuredWidgets.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedWallpaperId by app.repository.selectedWallpaperId.collectAsStateWithLifecycle(initialValue = "aurora")
    val proPriceLabel by app.billingManager.proPriceLabel.collectAsStateWithLifecycle()
    val billingStatus by app.billingManager.billingStatusMessage.collectAsStateWithLifecycle()

    val selectedWallpaper = remember(selectedWallpaperId) {
        WallpaperBackdrop.fromId(selectedWallpaperId)
    }

    var currentTab by remember { mutableStateOf(MainTab.GALLERY) }
    var editingConfig by remember {
        mutableStateOf(
            if (isLauncherConfigure) {
                WidgetConfig(
                    instanceId = "appWidget_$launchAppWidgetId",
                    appWidgetId = launchAppWidgetId,
                    templateId = launchTemplateExtra ?: WidgetTemplateId.FROSTED_DIGITAL_CLOCK
                )
            } else null
        )
    }
    var adFallbackTemplate by remember { mutableStateOf<WidgetTemplateId?>(null) }

    val triggerRewardedUnlock: (WidgetTemplateId) -> Unit = { template ->
        app.adMobManager.showRewardedAdToUnlock(
            activity = activity,
            onRewardEarned = {
                scope.launch {
                    app.repository.unlockTemplateViaRewardedAd(template)
                    snackbarHostState.showSnackbar("${template.title} unlocked permanently!")
                }
            },
            onUnavailable = {
                adFallbackTemplate = template
            }
        )
    }

    val activeEditorConfig = editingConfig
    if (activeEditorConfig != null) {
        WidgetEditorScreen(
            initialConfig = activeEditorConfig,
            isProUnlocked = isProUnlocked,
            selectedWallpaper = selectedWallpaper,
            isLauncherConfigureFlow = isLauncherConfigure,
            onSaveConfig = { saved ->
                app.repository.saveWidgetConfig(saved)
                refreshAllCategoryWidgets(activity.applicationContext)
            },
            onBack = {
                if (isLauncherConfigure) {
                    activity.finish()
                } else {
                    editingConfig = null
                }
            },
            onOpenProUpgrade = {
                editingConfig = null
                currentTab = MainTab.PRO
            },
            onAfterPinOrSave = {
                app.adMobManager.showGentleInterstitialAfterSave(activity, isProUnlocked) {
                    if (isLauncherConfigure) {
                        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, launchAppWidgetId)
                        activity.setResult(Activity.RESULT_OK, result)
                        activity.finish()
                    } else {
                        editingConfig = null
                        currentTab = MainTab.MY_WIDGETS
                    }
                }
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == MainTab.GALLERY,
                        onClick = { currentTab = MainTab.GALLERY },
                        icon = { Icon(Icons.Rounded.DashboardCustomize, contentDescription = null) },
                        label = { Text(MainTab.GALLERY.label) }
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.MY_WIDGETS,
                        onClick = { currentTab = MainTab.MY_WIDGETS },
                        icon = { Icon(Icons.Rounded.Widgets, contentDescription = null) },
                        label = { Text(MainTab.MY_WIDGETS.label) }
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.PRO,
                        onClick = { currentTab = MainTab.PRO },
                        icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
                        label = { Text(MainTab.PRO.label) }
                    )
                }
            }
        ) { innerPadding ->
            when (currentTab) {
                MainTab.GALLERY -> GalleryScreen(
                    isProUnlocked = isProUnlocked,
                    rewardedUnlocked = rewardedUnlocked,
                    selectedWallpaper = selectedWallpaper,
                    onSelectWallpaper = { backdrop ->
                        scope.launch { app.repository.setSelectedWallpaperId(backdrop.id) }
                    },
                    onOpenWidgetEditor = { template ->
                        val existing = configuredWidgets.firstOrNull { it.templateId == template }
                        editingConfig = existing ?: WidgetConfig(
                            instanceId = "preset_${template.name}_${UUID.randomUUID().toString().take(6)}",
                            templateId = template
                        )
                    },
                    onWatchRewardedAd = triggerRewardedUnlock,
                    onOpenProUpgrade = { currentTab = MainTab.PRO },
                    modifier = Modifier.padding(innerPadding)
                )

                MainTab.MY_WIDGETS -> MyWidgetsScreen(
                    configuredWidgets = configuredWidgets,
                    selectedWallpaper = selectedWallpaper,
                    onEditWidget = { config -> editingConfig = config },
                    onDeleteWidget = { config ->
                        scope.launch {
                            app.repository.deleteWidgetConfig(config.instanceId)
                            refreshAllCategoryWidgets(activity.applicationContext)
                        }
                    },
                    onRefreshAll = {
                        scope.launch {
                            refreshAllCategoryWidgets(activity.applicationContext)
                            snackbarHostState.showSnackbar("All home screen widgets refreshed.")
                        }
                    },
                    onBrowseGallery = { currentTab = MainTab.GALLERY },
                    modifier = Modifier.padding(innerPadding)
                )

                MainTab.PRO -> ProSettingsScreen(
                    isProUnlocked = isProUnlocked,
                    proPriceLabel = proPriceLabel,
                    rewardedUnlocked = rewardedUnlocked,
                    billingStatusMessage = billingStatus,
                    onPurchasePro = { app.billingManager.launchProPurchaseFlow(activity) },
                    onRestorePurchases = { app.billingManager.restorePurchases() },
                    onWatchAdForWidget = triggerRewardedUnlock,
                    onToggleDebugPro = { enabled ->
                        scope.launch { app.repository.setProUnlocked(enabled) }
                    },
                    onRefreshAllWidgets = {
                        scope.launch {
                            refreshAllCategoryWidgets(activity.applicationContext)
                            snackbarHostState.showSnackbar("Home screen widgets synced.")
                        }
                    },
                    onDismissBillingMessage = { app.billingManager.clearStatusMessage() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    adFallbackTemplate?.let { template ->
        AlertDialog(
            onDismissRequest = { adFallbackTemplate = null },
            title = { Text("Test Ad Unavailable Offline") },
            text = {
                Text(
                    "Google Test Rewarded Ad couldn't load (device may be offline or ad is still caching). " +
                        if (BuildConfig.DEBUG) {
                            "Since this is a local Debug build, you can simulate completing the ad to test unlocking ${template.title}."
                        } else {
                            "Please check your internet connection and try again in a moment."
                        }
                )
            },
            confirmButton = {
                if (BuildConfig.DEBUG) {
                    TextButton(onClick = {
                        val t = template
                        adFallbackTemplate = null
                        scope.launch {
                            app.repository.unlockTemplateViaRewardedAd(t)
                            snackbarHostState.showSnackbar("${t.title} unlocked (Debug test)!")
                        }
                    }) {
                        Text("Simulate Reward Unlock")
                    }
                } else {
                    TextButton(onClick = { adFallbackTemplate = null }) { Text("OK") }
                }
            },
            dismissButton = {
                TextButton(onClick = { adFallbackTemplate = null }) { Text("Close") }
            }
        )
    }
}
