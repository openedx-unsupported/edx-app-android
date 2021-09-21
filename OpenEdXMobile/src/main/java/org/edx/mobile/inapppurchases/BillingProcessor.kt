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
 * */
class BillingProcessor(val context: Context, val listener: BillingFlowListeners?) :
    PurchasesUpdatedListener,
    BillingClientStateListener {

    private val TAG = BillingProcessor::class.java.simpleName
    private val logger = Logger(TAG)

    private var RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
    private val RECONNECT_MAX_TIME = 3 // retry connect max times

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
        if (connectionTryCount > RECONNECT_MAX_TIME) {
            connectionTryCount++
            retryBillingServiceConnectionWithExponentialBackoff()
        } else {
            listener?.onBillingServiceDisconnected()
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (purchases != null && !purchases[0].isAcknowledged) {
            acknowledgePurchase(purchases[0])
        }
    }

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

    private fun launchBillingFlow(activity: Activity, skuDetail: SkuDetails) {
        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
        billingFlowParamsBuilder.setSkuDetails(skuDetail)
        billingClient.launchBillingFlow(
            activity, billingFlowParamsBuilder.build()
        )
    }

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

    private fun querySyncDetails(productId: String, listener: SkuDetailsResponseListener) {
        billingClient.querySkuDetailsAsync(
            SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(listOf(productId))
                .build(), listener
        )
    }

    fun disconnect() {
        billingClient.endConnection()
    }

    interface BillingFlowListeners {
        fun onBillingServiceDisconnected() {}

        fun onBillingSetupFinished(billingResult: BillingResult) {}

        fun onPurchaseComplete(purchase: Purchase)
    }
}
