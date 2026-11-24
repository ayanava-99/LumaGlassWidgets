package com.lumaglass.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumaglass.widgets.data.AppColorTheme
import com.lumaglass.widgets.data.WidgetConfig
import com.lumaglass.widgets.data.WidgetTemplateId
import com.lumaglass.widgets.glance.refreshAllCategoryWidgets
import com.lumaglass.widgets.ui.components.ShareHelper
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
    MY_WIDGETS("Saved"),
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
            val selectedAppThemeId by app.repository.selectedAppThemeId
                .collectAsStateWithLifecycle(initialValue = AppColorTheme.PIXEL_BLUE.id)
            val appColorTheme = remember(selectedAppThemeId) {
                AppColorTheme.fromId(selectedAppThemeId)
            }

            LumaGlassTheme(appTheme = appColorTheme) {
                LumaGlassRootScreen(
                    activity = this,
                    app = app,
                    appColorTheme = appColorTheme,
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
    appColorTheme: AppColorTheme,
    launchAppWidgetId: Int,
    isLauncherConfigure: Boolean,
    launchTemplateExtra: WidgetTemplateId?
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isProUnlocked by app.repository.isProUnlocked.collectAsStateWithLifecycle(initialValue = false)
    val rewardedUnlocked by app.repository.rewardedUnlockedTemplates.collectAsStateWithLifecycle(initialValue = emptySet())
    val favoriteTemplates by app.repository.favoriteTemplates.collectAsStateWithLifecycle(initialValue = emptySet())
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
                    templateId = launchTemplateExtra ?: WidgetTemplateId.MINIMAL_PILL_CLOCK
                )
            } else null
        )
    }

    // Bug fix: When a user taps an already-pinned widget on their home screen to reconfigure it,
    // load their saved configuration from DataStore instead of overwriting it with default values.
    LaunchedEffect(isLauncherConfigure, launchAppWidgetId) {
        if (isLauncherConfigure && launchAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val existing = app.repository.getWidgetConfigById("appWidget_$launchAppWidgetId")
            if (existing != null) {
                editingConfig = existing
            }
        }
    }

    // Bug fix: Intercept system back gesture when inside the in-app WidgetEditorScreen so
    // pressing Back returns to the Gallery instead of exiting the app.
    BackHandler(enabled = editingConfig != null && !isLauncherConfigure) {
        editingConfig = null
    }

    var adFallbackTemplate by remember { mutableStateOf<WidgetTemplateId?>(null) }
    var adStylePassCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showReviewPromptDialog by remember { mutableStateOf(false) }

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
                if (app.repository.incrementSaveCountAndCheckReviewPrompt()) {
                    showReviewPromptDialog = true
                }
            },
            onBack = {
                if (isLauncherConfigure) {
                    activity.finish()
                } else {
                    editingConfig = null
                }
            },
            onWatchAdForStylePass = { onGranted ->
                app.adMobManager.showRewardedAdToUnlock(
                    activity = activity,
                    onRewardEarned = onGranted,
                    onUnavailable = {
                        adStylePassCallback = onGranted
                    }
                )
            },
            onPurchaseProDirect = {
                app.billingManager.launchProPurchaseFlow(activity)
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
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = Dp.Hairline
                ) {
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
                    favoriteTemplates = favoriteTemplates,
                    selectedWallpaper = selectedWallpaper,
                    selectedAppTheme = appColorTheme,
                    onSelectWallpaper = { backdrop ->
                        scope.launch { app.repository.setSelectedWallpaperId(backdrop.id) }
                    },
                    onSelectAppTheme = { theme ->
                        scope.launch { app.repository.setSelectedAppThemeId(theme.id) }
                    },
                    onToggleFavorite = { template ->
                        scope.launch { app.repository.toggleFavoriteTemplate(template) }
                    },
                    onOpenWidgetEditor = { template ->
                        val existing = configuredWidgets.firstOrNull { it.templateId == template }
                        editingConfig = existing ?: WidgetConfig(
                            instanceId = "preset_${template.name}_${UUID.randomUUID().toString().take(6)}",
                            templateId = template
                        )
                    },
                    onOpenCuratedPack = { pack ->
                        editingConfig = pack.toConfig(
                            instanceId = "preset_${pack.templateId.name}_${UUID.randomUUID().toString().take(6)}"
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
                    selectedAppTheme = appColorTheme,
                    selectedWallpaper = selectedWallpaper,
                    billingStatusMessage = billingStatus,
                    onSelectAppTheme = { theme ->
                        scope.launch { app.repository.setSelectedAppThemeId(theme.id) }
                    },
                    onSelectWallpaper = { backdrop ->
                        scope.launch { app.repository.setSelectedWallpaperId(backdrop.id) }
                    },
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

    adStylePassCallback?.let { onGranted ->
        AlertDialog(
            onDismissRequest = { adStylePassCallback = null },
            title = { Text("Ad Unavailable Offline") },
            text = {
                Text(
                    if (BuildConfig.DEBUG) {
                        "Google Test Rewarded Ad couldn't load offline. Simulate watching the ad to test pinning with Pro styling?"
                    } else {
                        "Please check your internet connection to load the rewarded ad, or unlock Lifetime Pro (₹49)."
                    }
                )
            },
            confirmButton = {
                if (BuildConfig.DEBUG) {
                    TextButton(onClick = {
                        adStylePassCallback = null
                        onGranted()
                    }) {
                        Text("Simulate Reward & Pin")
                    }
                } else {
                    TextButton(onClick = { adStylePassCallback = null }) { Text("OK") }
                }
            },
            dismissButton = {
                TextButton(onClick = { adStylePassCallback = null }) { Text("Cancel") }
            }
        )
    }

    if (showReviewPromptDialog && editingConfig == null) {
        AlertDialog(
            onDismissRequest = { showReviewPromptDialog = false },
            title = { Text("Enjoying LumaGlass? ⭐") },
            text = {
                Text(
                    "Your widgets are looking clean! If LumaGlass made your home screen better, a quick 5-star rating on Google Play helps us grow and add more widgets."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showReviewPromptDialog = false
                    ShareHelper.openPlayStoreListing(activity)
                }) {
                    Text("Rate 5★ on Play Store")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewPromptDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }
}
