package com.lumaglass.widgets.monetization

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.lumaglass.widgets.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages AdMob Rewarded Ads (widget unlock) and gentle post-save Interstitial Ads.
 * Unit IDs are configured centrally in gradle.properties (defaults to Google Test IDs).
 */
class AdMobManager(private val context: Context) {

    companion object {
        val TEST_REWARDED_AD_UNIT_ID: String = BuildConfig.ADMOB_REWARDED_ID
        val TEST_INTERSTITIAL_AD_UNIT_ID: String = BuildConfig.ADMOB_INTERSTITIAL_ID
    }

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false
    private var interstitialShownCount = 0

    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    fun initialize() {
        if (isInitialized) return
        isInitialized = true
        runCatching {
            MobileAds.initialize(context) {
                loadRewardedAd()
                loadInterstitialAd()
            }
        }
    }

    fun loadRewardedAd() {
        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isRewardedReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    _isRewardedReady.value = false
                }
            }
        )
    }

    fun loadInterstitialAd() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showRewardedAdToUnlock(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onUnavailable: (String) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewardedAd()
            onUnavailable("Test rewarded ad is still loading or device is offline.")
            return
        }

        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _isRewardedReady.value = false
                loadRewardedAd()
                if (earned) {
                    onRewardEarned()
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _isRewardedReady.value = false
                loadRewardedAd()
                onUnavailable(error.message)
            }
        }

        ad.show(activity) {
            earned = true
        }
    }

    /**
     * Gentle interstitial shown only after saving/pinning a widget, skipped for Pro users,
     * and capped so it never feels spammy.
     */
    fun showGentleInterstitialAfterSave(
        activity: Activity,
        isPro: Boolean,
        onFinished: () -> Unit
    ) {
        if (isPro || interstitialShownCount >= 2) {
            onFinished()
            return
        }
        val ad = interstitialAd
        if (ad == null) {
            loadInterstitialAd()
            onFinished()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                interstitialShownCount++
                loadInterstitialAd()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitialAd()
                onFinished()
            }
        }
        ad.show(activity)
    }
}
