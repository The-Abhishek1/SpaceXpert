package com.xcloak.spacexpert.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object BillingManager {
    const val PRODUCT_ID_PRO = "spacexpert_pro_subscription"

    private val _isProActive = MutableStateFlow(false)
    val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun initialize(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()

        connectToPlayBilling()
    }

    private fun connectToPlayBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection logic can be added here if needed
            }
        })
    }

    fun queryPurchases() {
        val client = billingClient ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPro = purchases.any { purchase ->
                    purchase.products.contains(PRODUCT_ID_PRO) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _isProActive.value = hasPro
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val client = billingClient ?: return
        
        // For testing/mocking purposes, or when working in debug environments, if BillingClient cannot find the live product ID, 
        // we can gracefully simulate a successful purchase or fall back to a mock activation so the user can test the Pro features easily.
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PRO)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList.first()
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                        )
                    )
                    .build()
                
                client.launchBillingFlow(activity, flowParams)
            } else {
                // Only allow the automatic toggle bypass during local debugging / development
                if (com.xcloak.spacexpert.BuildConfig.DEBUG) {
                    _isProActive.value = !_isProActive.value
                }
            }
        }
    }

    // Call this for direct developer/manual toggle if needed or during testing
    fun toggleProStateDebug() {
        if (com.xcloak.spacexpert.BuildConfig.DEBUG) {
            _isProActive.value = !_isProActive.value
        }
    }
}

private fun handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        if (purchase.products.contains(BillingManager.PRODUCT_ID_PRO)) {
            BillingManager.queryPurchases()
        }
    }
}
