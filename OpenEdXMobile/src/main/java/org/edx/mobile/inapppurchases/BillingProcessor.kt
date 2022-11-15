package org.edx.mobile.inapppurchases

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.extenstion.encodeToString
import org.edx.mobile.logger.Logger
import javax.inject.Inject

/**
 * The BillingProcessor implements all billing functionality for application.
 * Purchases can happen while in the app.
 *
 * This BillingProcessor knows nothing about the application, all necessary information is either
 * passed into the constructor, exported as observable Flows, or exported through callbacks.
 *
 * Inspiration: [https://github.com/android/play-billing-samples/blob/master/TrivialDriveKotlin/app/src/main/java/com/sample/android/trivialdrivesample/billing/BillingDataSource.kt]
 * */
@Module
@InstallIn(ActivityComponent::class)
class BillingProcessor @Inject constructor(@ApplicationContext val context: Context) :
    PurchasesUpdatedListener, BillingClientStateListener {

    private val logger = Logger(TAG)

    private val handler = Handler(Looper.getMainLooper())
    private var connectionTryCount = 0

    private lateinit var listener: BillingFlowListeners

    // Billing client, connection, cached data
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun setUpBillingFlowListeners(listener: BillingFlowListeners) {
        this.listener = listener
    }

    fun startConnection() {
        if (!isConnected()) {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        logger.debug(
            "BillingSetupFinished -> Response code: " + billingResult.responseCode.toString() +
                    " Debug message: " + billingResult.debugMessage
        )
        listener.onBillingSetupFinished(billingResult)
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
            listener.onBillingServiceDisconnected()
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
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            if (!purchases[0].isAcknowledged) {
                acknowledgePurchase(purchases[0])
            } else {
                listener.onPurchaseComplete(purchases[0])
            }
        } else {
            listener.onPurchaseCancel(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    /**
     * Called to purchase the new product. Query the product details and launch the purchase flow.
     *
     * @param activity active activity to launch our billing flow from
     * @param productId SKU (Product ID) to be purchased
     * @param userId    User Id of the purchaser
     */
    fun purchaseItem(activity: Activity, productId: String, userId: Long) {
        startConnection()
        if (billingClient.isReady) {
            querySyncDetails(productId) { billingResult, skuDetailsList ->
                logger.debug(
                    "Getting Purchases -> Response code: " + billingResult.responseCode.toString() +
                            " Debug message: " + billingResult.debugMessage
                )
                if (skuDetailsList != null) {
                    launchBillingFlow(activity, skuDetailsList[0], userId)
                }
            }
        }
    }

    /**
     * Launch the billing flow. This will launch an external Activity for a result, so it requires
     * an Activity reference.
     *
     * @param activity active activity to launch our billing flow from
     * @param skuDetail SKU (Product) to be purchased
     * @param userId    User Id of the purchaser
     */
    private fun launchBillingFlow(activity: Activity, skuDetail: SkuDetails, userId: Long) {
        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
        billingFlowParamsBuilder.setSkuDetails(skuDetail)
        billingFlowParamsBuilder.setObfuscatedAccountId(userId.encodeToString())
        billingClient.launchBillingFlow(
            activity, billingFlowParamsBuilder.build()
        )
    }

    /**
     * Acknowledge new purchases are ones not yet acknowledged.
     * @param purchase new purchase
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        startConnection()
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
                listener.onPurchaseComplete(purchase)
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

    /**
     * Calls the billing client functions to query sku details for in-app SKUs. SKU details are
     * useful for displaying item names and price lists to the user, and are required to make a
     * purchase.
     *
     * @param productId SKU of the product
     * @param listener [SkuDetailsResponseListener]
     * */
    fun querySyncDetails(productId: String, listener: SkuDetailsResponseListener) {
        startConnection()
        billingClient.querySkuDetailsAsync(
            SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(listOf(productId))
                .build(), listener
        )
    }

    /**
     * Method to query the Purchases async and returns purchases details for currently owned items
     * bought within the app.
     *
     * @param listener callback interface to get the Purchases.
     * */
    fun queryPurchase(listener: PurchasesResponseListener) {
        startConnection()
        billingClient.queryPurchasesAsync(
            BillingClient.SkuType.INAPP, listener
        )
    }

    fun isConnected(): Boolean {
        return billingClient.isReady
    }

    /**
     * Closes the connection and releases all held resources such as service connections.
     */
    fun disconnect() {
        billingClient.endConnection()
    }

    companion object {
        private val TAG = BillingProcessor::class.java.simpleName
        private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
        private const val RECONNECT_MAX_COUNT = 3 // retry connection max count
    }

    interface BillingFlowListeners {
        fun onBillingServiceDisconnected() {}

        fun onBillingSetupFinished(billingResult: BillingResult) {}

        fun onPurchaseCancel(responseCode: Int, message: String) {}

        fun onPurchaseComplete(purchase: Purchase) {}
    }
}
