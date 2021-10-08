package org.edx.mobile.inapppurchases

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.*
import org.edx.mobile.logger.Logger

/**
 * The BillingProcessor implements all billing functionality for application.
 * Purchases can happen while in the app.
 *
 * This BillingProcessor knows nothing about the application, all necessary information is either
 * passed into the constructor, exported as observable Flows, or exported through callbacks.
 *
 * Inspiration: [https://github.com/android/play-billing-samples/blob/master/TrivialDriveKotlin/app/src/main/java/com/sample/android/trivialdrivesample/billing/BillingDataSource.kt]
 * */
class BillingProcessor(val context: Context, val listener: BillingFlowListeners?) :
    PurchasesUpdatedListener,
    BillingClientStateListener {

    private val TAG = BillingProcessor::class.java.simpleName
    private val logger = Logger(TAG)

    private var RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
    private val RECONNECT_MAX_COUNT = 3 // retry connection max count

    private val handler = Handler(Looper.getMainLooper())
    private var connectionTryCount = 0

    // Billing client, connection, cached data
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        logger.debug(
            "BillingSetupFinished -> Response code: " + billingResult.responseCode.toString() +
                    " Debug message: " + billingResult.debugMessage
        )
        listener?.onBillingSetupFinished(billingResult)
    }

    /**
     * This is a pretty unusual occurrence. It happens primarily if the Google Play Store
     * self-upgrades or is force closed.
     */
    override fun onBillingServiceDisconnected() {
        if (connectionTryCount > RECONNECT_MAX_COUNT) {
            connectionTryCount++
            retryBillingServiceConnectionWithExponentialBackoff()
        } else {
            listener?.onBillingServiceDisconnected()
        }
    }

    /**
     * Called by the BillingLibrary when new purchases are detected; typically in response to a
     * launchBillingFlow.
     * @param billingResult result of the purchase flow.
     * @param list of new purchases.
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (purchases != null && !purchases[0].isAcknowledged) {
            acknowledgePurchase(purchases[0])
        } else if (purchases == null) {
            listener?.onPurchaseCancel()
        }
    }

    /**
     * Called to purchase the new product. Query the product details and launch the purchase flow.
     *
     * @param activity active activity to launch our billing flow from
     * @param productId SKU (Product ID) to be purchased
     */
    fun purchaseItem(activity: Activity, productId: String) {
        if (billingClient.isReady) {

            // this block of code is only for test purpose only have to remove before merging it.
            billingClient.queryPurchasesAsync(
                BillingClient.SkuType.INAPP
            ) { _, purchases ->
                if (purchases.size > 0) {
                    consumePurchase(purchases[0].purchaseToken)
                }
            }

            querySyncDetails(productId,
                SkuDetailsResponseListener { billingResult, skuDetailsList ->
                    logger.debug(
                        "Getting Purchases -> Response code: " + billingResult.responseCode.toString() +
                                " Debug message: " + billingResult.debugMessage
                    )
                    if (skuDetailsList != null) {
                        launchBillingFlow(activity, skuDetailsList[0])
                    }
                })
        }
    }

    /**
     * Launch the billing flow. This will launch an external Activity for a result, so it requires
     * an Activity reference.
     *
     * @param activity active activity to launch our billing flow from
     * @param skuDetail SKU (Product) to be purchased
     */
    private fun launchBillingFlow(activity: Activity, skuDetail: SkuDetails) {
        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
        billingFlowParamsBuilder.setSkuDetails(skuDetail)
        billingClient.launchBillingFlow(
            activity, billingFlowParamsBuilder.build()
        )
    }

    /**
     * Acknowledge new purchases are ones not yet acknowledged.
     * @param purchase new purchase
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                logger.debug(
                    "acknowledgePurchase -> " +
                            "Response code: " + billingResult.responseCode.toString() +
                            " Debug message: " + billingResult.debugMessage
                )
                listener?.onPurchaseComplete(purchase)
            }
        }
    }

    /**
     * Retries the billing service connection with exponential backoff.
     */
    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(
            { billingClient.startConnection(this@BillingProcessor) },
            RECONNECT_TIMER_START_MILLISECONDS
        )
    }

    private fun consumePurchase(purchaseToken: String) {
        billingClient.consumeAsync(
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        ) { billingResult, _ -> logger.debug(billingResult.responseCode.toString() + billingResult.debugMessage) }
    }

    /**
     * Calls the billing client functions to query sku details for inapp SKUs. SKU details are
     * useful for displaying item names and price lists to the user, and are required to make a
     * purchase.
     *
     * @param productId SKU of the product
     * @param listener [SkuDetailsResponseListener]
     * */
    private fun querySyncDetails(productId: String, listener: SkuDetailsResponseListener) {
        billingClient.querySkuDetailsAsync(
            SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(listOf(productId))
                .build(), listener
        )
    }

    /**
     * Closes the connection and releases all held resources such as service connections.
     */
    fun disconnect() {
        billingClient.endConnection()
    }

    interface BillingFlowListeners {
        fun onBillingServiceDisconnected() {}

        fun onBillingSetupFinished(billingResult: BillingResult) {}

        fun onPurchaseCancel()

        fun onPurchaseComplete(purchase: Purchase)
    }
}
