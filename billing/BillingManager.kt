package com.itpdf.app.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * BillingManager handles the Google Play Billing integration for IT PDF.
 * It manages the "Pro Mode" one-time purchase, unlocking premium features.
 */
class BillingManager(context: Context) : PurchasesUpdatedListener {

    private val TAG = "BillingManager"
    private val app浸Context = context.applicationContext
    
    companion object {
        const val PRODUCT_PRO_MODE = "it_pdf_pro_permanent"
        private const val RECONNECT_DELAY_MS = 3000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var billingClient: BillingClient = BillingClient.newBuilder(app浸Context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _isProPurchased = MutableStateFlow(false)
    val isProPurchased: StateFlow<Boolean> = _isProPurchased.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady.asStateFlow()

    init {
        startConnection()
    }

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client Connection Success")
                    _isBillingReady.value = true
                    queryProductDetails()
                    checkPurchases()
                } else {
                    Log.e(TAG, "Billing Setup Failed: ${billingResult.debugMessage}")
                    _isBillingReady.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing Service Disconnected. Retrying in ${RECONNECT_DELAY_MS}ms...")
                _isBillingReady.value = false
                scope.launch {
                    delay(RECONNECT_DELAY_MS)
                    startConnection()
                }
            }
        })
    }

    private fun queryProductDetails() {
        if (!billingClient.isReady) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PRO_MODE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val proProduct = productDetailsList.find { it.productId == PRODUCT_PRO_MODE }
                _productDetails.value = proProduct
                Log.d(TAG, "Product details loaded: ${proProduct?.name}")
            } else {
                Log.e(TAG, "Query Product Details Failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun checkPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isPurchased = purchases.any { purchase ->
                    purchase.products.contains(PRODUCT_PRO_MODE) && 
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _isProPurchased.value = isPurchased
                
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        val details = _productDetails.value
        if (details == null) {
            Log.e(TAG, "Product details not available")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "User canceled the purchase flow")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _isProPurchased.value = true
                checkPurchases()
            }
            else -> {
                Log.e(TAG, "Purchase Error: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                if (purchase.products.contains(PRODUCT_PRO_MODE)) {
                    _isProPurchased.value = true
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase Acknowledged")
                if (purchase.products.contains(PRODUCT_PRO_MODE)) {
                    _isProPurchased.value = true
                }
            } else {
                Log.e(TAG, "Acknowledgment Failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}