package com.lumaglass.widgets.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.lumaglass.widgets.BuildConfig
import com.lumaglass.widgets.data.LumaPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Google Play Billing Library v7+ manager for the ₹49 one-time lifetime Pro unlock.
 * Product ID: [PRODUCT_ID_PRO_LIFETIME]
 */
class BillingManager(
    context: Context,
    private val repository: LumaPreferencesRepository
) : PurchasesUpdatedListener {

    companion object {
        val PRODUCT_ID_PRO_LIFETIME: String = BuildConfig.PLAY_PRO_PRODUCT_ID
        const val DISPLAY_PRICE_FALLBACK = "₹49"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _proPriceLabel = MutableStateFlow(DISPLAY_PRICE_FALLBACK)
    val proPriceLabel: StateFlow<String> = _proPriceLabel.asStateFlow()

    private val _billingStatusMessage = MutableStateFlow<String?>(null)
    val billingStatusMessage: StateFlow<String?> = _billingStatusMessage.asStateFlow()

    private var proProductDetails: ProductDetails? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun startConnection() {
        if (billingClient.isReady) {
            scope.launch {
                queryProProductDetails()
                restorePurchases()
            }
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryProProductDetails()
                        restorePurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Will reconnect automatically on next purchase or restore attempt
            }
        })
    }

    private suspend fun queryProProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PRO_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        val result = billingClient.queryProductDetails(params)
        val details = result.productDetailsList?.firstOrNull()
        if (details != null) {
            proProductDetails = details
            details.oneTimePurchaseOfferDetails?.formattedPrice?.let { formatted ->
                _proPriceLabel.value = formatted
            }
        }
    }

    fun launchProPurchaseFlow(activity: Activity) {
        if (!billingClient.isReady) {
            startConnection()
            _billingStatusMessage.value = "Play Billing not ready yet. Ensure Play Store account is active or test in Play Console internal testing."
            return
        }
        val details = proProductDetails
        if (details == null) {
            scope.launch { queryProProductDetails() }
            _billingStatusMessage.value = "Product '$PRODUCT_ID_PRO_LIFETIME' not found on Play Console yet. Configure it in Play Console or use Local Test Unlock below."
            return
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }
        scope.launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            val result = billingClient.queryPurchasesAsync(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(result.purchasesList)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    scope.launch { handlePurchases(purchases) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingStatusMessage.value = "Purchase cancelled."
            }
            else -> {
                _billingStatusMessage.value = "Billing response: ${result.debugMessage}"
            }
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        var hasPro = false
        for (purchase in purchases) {
            if (purchase.products.contains(PRODUCT_ID_PRO_LIFETIME) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            ) {
                hasPro = true
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams)
                }
            }
        }
        if (hasPro) {
            repository.setProUnlocked(true)
            _billingStatusMessage.value = "LumaGlass Pro Lifetime unlocked!"
        }
    }

    fun clearStatusMessage() {
        _billingStatusMessage.value = null
    }
}
