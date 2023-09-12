package org.edx.mobile.inapppurchases

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.edx.mobile.extenstion.encodeToString
import org.edx.mobile.extenstion.resumeIfActive
import org.edx.mobile.injection.DataSourceDispatcher
import org.edx.mobile.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The BillingProcessor implements all billing functionality for application.
 * Purchases can happen while in the app.
 *
 * This BillingProcessor knows nothing about the application, all necessary information is either
 * passed into the constructor, exported as observable Flows, or exported through callbacks.
 *
 * Inspiration: [https://github.com/android/play-billing-samples/blob/master/TrivialDriveKotlin/app/src/main/java/com/sample/android/trivialdrivesample/billing/BillingDataSource.kt]
 * */
@Singleton
class BillingProcessor @Inject constructor(
    @ApplicationContext val context: Context,
    @DataSourceDispatcher val dispatcher: CoroutineDispatcher,
) : PurchasesUpdatedListener {

    private val logger = Logger(TAG)

    private val handler = Handler(Looper.getMainLooper())
    private var connectionTryCount = 0

    private lateinit var listener: BillingFlowListeners
    private lateinit var billingClientStateListener: BillingClientStateListener

    // Billing client, connection, cached data
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun setUpBillingFlowListeners(listener: BillingFlowListeners) {
        this.listener = listener
    }

    private suspend fun isReadyOrConnect(): Boolean {
        return billingClient.isReady || connect()
    }

    private suspend fun connect(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            billingClientStateListener = object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    logger.debug("BillingSetupFinished -> $billingResult")
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        continuation.resumeIfActive(true)
                    } else {
                        continuation.resumeIfActive(false)
                    }
                }

                /**
                 * This is a pretty unusual occurrence. It happens primarily if the Google Play
                 * Store self-upgrades or is force closed.
                 */
                override fun onBillingServiceDisconnected() {
                    if (connectionTryCount > RECONNECT_MAX_COUNT) {
                        connectionTryCount++
                        retryBillingServiceConnectionWithExponentialBackoff()
                    }
                    continuation.resumeIfActive(false)
                }
            }
            billingClient.startConnection(billingClientStateListener)
        }
    }

    /**
     * Called by the BillingLibrary when new purchases are detected; typically in response to a
     * launch billing flow.
     * @param billingResult result of the purchase flow.
     * @param purchases list of new purchases.
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            if (!purchases.first().isAcknowledged) {
                CoroutineScope(dispatcher).launch {
                    acknowledgePurchase(purchases.first())
                }
            } else {
                listener.onPurchaseComplete(purchases.first())
            }
        } else {
            listener.onPurchaseCancel(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    /**
     * Called to purchase the new product. Query the product details and launch the purchase flow.
     *
     * @param activity active activity to launch our billing flow from
     * @param productId Product Id to be purchased
     * @param userId    User Id of the purchaser
     */
    suspend fun purchaseItem(activity: Activity, productId: String, userId: Long) {
        if (isReadyOrConnect()) {
            val response = querySyncDetails(productId)
            logger.debug("Getting Purchases -> ${response.billingResult}")

            response.productDetailsList?.first()?.let {
                launchBillingFlow(activity, it, userId)
            }
        } else {
            listener.onPurchaseCancel(BillingResponseCode.BILLING_UNAVAILABLE, "")
        }
    }

    /**
     * Launch the billing flow. This will launch an external Activity for a result, so it requires
     * an Activity reference.
     *
     * @param activity active activity to launch our billing flow from
     * @param productDetails Product to be purchased
     * @param userId    User Id of the purchaser
     */
    private fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        userId: Long
    ) {
        val productDetailsParamsList = listOf(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId(userId.encodeToString())
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Acknowledge new purchases are ones not yet acknowledged.
     * @param purchase new purchase
     */
    private suspend fun acknowledgePurchase(purchase: Purchase) {
        isReadyOrConnect()
        val billingResult = billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
        if (billingResult.responseCode == BillingResponseCode.OK) {
            logger.debug("acknowledgePurchase -> $billingResult")
            listener.onPurchaseComplete(purchase)
        }
    }

    /**
     * Retries the billing service connection with exponential backoff.
     */
    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(
            { billingClient.startConnection(billingClientStateListener) },
            RECONNECT_TIMER_START_MILLISECONDS
        )
    }

    /**
     * Calls the billing client functions to query product details for in-app products. Product
     * details are useful for displaying item names and price lists to the user, and are required to
     * make purchase.
     *
     * @param productId Id of the product
     * @return Details of the product
     * */
    suspend fun querySyncDetails(productId: String): ProductDetailsResult {
        isReadyOrConnect()
        val productDetails = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        return withContext(dispatcher) {
            billingClient.queryProductDetails(
                QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(listOf(productDetails))
                    .build()
            )
        }
    }

    /**
     * Method to query the Purchases async and returns purchases for currently owned items
     * bought within the app.
     *
     * @return List of purchases
     **/
    suspend fun queryPurchases(): List<Purchase> {
        isReadyOrConnect()
        return billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ).purchasesList
    }

    companion object {
        private val TAG = BillingProcessor::class.java.simpleName
        private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
        private const val RECONNECT_MAX_COUNT = 3 // retry connection max count
        const val MICROS_TO_UNIT = 1_000_000 // 1,000,000 micro-units equal one unit of the currency
    }

    interface BillingFlowListeners {
        fun onPurchaseCancel(responseCode: Int, message: String) {}

        fun onPurchaseComplete(purchase: Purchase) {}
    }
}

fun ProductDetails.OneTimePurchaseOfferDetails.getPriceAmount(): Double =
    this.priceAmountMicros.toDouble().div(BillingProcessor.MICROS_TO_UNIT)
