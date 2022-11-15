package org.edx.mobile.viewModel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.decodeToLong
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.api.getAuditCoursesSku
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.repository.InAppPurchasesRepository
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.InAppPurchasesUtils
import javax.inject.Inject

@HiltViewModel
class InAppPurchasesViewModel @Inject constructor(
    private val billingProcessor: BillingProcessor,
    private val repository: InAppPurchasesRepository,
    private val iapAnalytics: InAppPurchasesAnalytics
) : ViewModel() {

    private val _productPrice = MutableLiveData<SkuDetails>()
    val productPrice: LiveData<SkuDetails> = _productPrice

    private val _productPurchased = MutableLiveData<Purchase>()
    val productPurchased: LiveData<Purchase> = _productPurchased

    private val _executeResponse = MutableLiveData<ExecuteOrderResponse>()
    val executeResponse: LiveData<ExecuteOrderResponse> = _executeResponse

    private val _showFullscreenLoaderDialog = MutableLiveData(false)
    val showFullscreenLoaderDialog: LiveData<Boolean> = _showFullscreenLoaderDialog

    private val _purchaseFlowComplete = MutableLiveData(false)
    val purchaseFlowComplete: LiveData<Boolean> = _purchaseFlowComplete

    private val _refreshCourseData = MutableLiveData(false)
    val refreshCourseData: LiveData<Boolean> = _refreshCourseData

    private val _completedUnfulfilledPurchase = MutableLiveData<Boolean>()
    val completedUnfulfilledPurchase: LiveData<Boolean> = _completedUnfulfilledPurchase

    private val _showLoader = MutableLiveData(false)
    val showLoader: LiveData<Boolean> = _showLoader

    private val _errorMessage = MutableLiveData<ErrorMessage?>()
    val errorMessage: LiveData<ErrorMessage?> = _errorMessage

    var upgradeMode = UpgradeMode.NORMAL

    private var _productId: String = ""
    val productId: String
        get() = _productId

    private var _isVerificationPending = true
    val isVerificationPending: Boolean
        get() = _isVerificationPending

    private var basketId: Long = 0
    private var purchaseToken: String = ""
    private var incompletePurchases: MutableList<Pair<String, String>> = arrayListOf()
    private var userId: Long = 0

    private val listener = object : BillingProcessor.BillingFlowListeners {
        override fun onPurchaseCancel(responseCode: Int, message: String) {
            super.onPurchaseCancel(responseCode, message)
            dispatchError(
                requestType = ErrorMessage.PAYMENT_SDK_CODE,
                throwable = InAppPurchasesException(responseCode, message)
            )
            endLoading()
        }

        override fun onPurchaseComplete(purchase: Purchase) {
            super.onPurchaseComplete(purchase)
            purchaseToken = purchase.purchaseToken
            _productPurchased.postValue(purchase)
            iapAnalytics.trackIAPEvent(eventName = Analytics.Events.IAP_PAYMENT_TIME)
            iapAnalytics.initUnlockContentTime()
        }
    }

    init {
        billingProcessor.setUpBillingFlowListeners(listener)
        billingProcessor.startConnection()
    }

    fun initializeProductPrice(courseSku: String?) {
        iapAnalytics.initPriceTime()
        courseSku?.let {
            billingProcessor.querySyncDetails(
                productId = courseSku
            ) { billingResult, skuDetails ->
                val skuDetail = skuDetails?.get(0)
                skuDetail?.let {
                    if (it.sku == courseSku) {
                        _productPrice.postValue(it)
                        iapAnalytics.setPrice(skuDetail.price)
                        iapAnalytics.trackIAPEvent(Analytics.Events.IAP_LOAD_PRICE_TIME)
                    }
                } ?: dispatchError(
                    requestType = ErrorMessage.PRICE_CODE,
                    throwable = InAppPurchasesException(
                        httpErrorCode = billingResult.responseCode,
                        errorMessage = billingResult.debugMessage,
                    )
                )
            }
        } ?: dispatchError(requestType = ErrorMessage.PRICE_CODE)
    }

    fun addProductToBasket(activity: Activity, userId: Long, productId: String) {
        this._productId = productId
        this.userId = userId
        startLoading()
        repository.addToBasket(
            productId = productId,
            callback = object : NetworkResponseCallback<AddToBasketResponse> {
                override fun onSuccess(result: Result.Success<AddToBasketResponse>) {
                    result.data?.let {
                        proceedCheckout(activity, it.basketId)
                    } ?: endLoading()
                }

                override fun onError(error: Result.Error) {
                    dispatchError(
                        requestType = ErrorMessage.ADD_TO_BASKET_CODE,
                        throwable = error.throwable
                    )
                    endLoading()
                }
            })
    }

    fun proceedCheckout(activity: Activity, basketId: Long) {
        this.basketId = basketId
        repository.proceedCheckout(
            basketId = basketId,
            callback = object : NetworkResponseCallback<CheckoutResponse> {
                override fun onSuccess(result: Result.Success<CheckoutResponse>) {
                    result.data?.let {
                        if (upgradeMode.isSilentMode()) {
                            executeOrder(activity)
                        } else {
                            iapAnalytics.initPaymentTime()
                            billingProcessor.purchaseItem(activity, productId, userId)
                        }
                    } ?: endLoading()
                }

                override fun onError(error: Result.Error) {
                    dispatchError(
                        requestType = ErrorMessage.CHECKOUT_CODE,
                        throwable = error.throwable
                    )
                    endLoading()
                }
            })
    }

    fun executeOrder(activity: Activity) {
        _isVerificationPending = false
        repository.executeOrder(
            basketId = basketId,
            productId = productId,
            purchaseToken = purchaseToken,
            callback = object : NetworkResponseCallback<ExecuteOrderResponse> {
                override fun onSuccess(result: Result.Success<ExecuteOrderResponse>) {
                    result.data?.let {
                        orderExecuted()
                        if (upgradeMode.isSilentMode()) {
                            markPurchaseComplete(activity)
                            _executeResponse.postValue(it)
                        } else {
                            refreshCourseData(true)
                        }
                    }
                    endLoading()
                }

                override fun onError(error: Result.Error) {
                    dispatchError(
                        requestType = ErrorMessage.EXECUTE_ORDER_CODE,
                        throwable = error.throwable
                    )
                    endLoading()
                }
            })
    }

    fun detectUnfulfilledPurchase(
        activity: Activity,
        userId: Long,
        enrolledCourses: List<EnrolledCoursesResponse>
    ) {
        val auditCoursesSku = enrolledCourses.getAuditCoursesSku()
        if (auditCoursesSku.isEmpty()) {
            _completedUnfulfilledPurchase.postValue(true)
            return
        }
        this.userId = userId
        billingProcessor.queryPurchase { _, purchases ->
            if (purchases.isEmpty()) {
                return@queryPurchase
            }
            val purchasesList =
                purchases.filter { it.accountIdentifiers?.obfuscatedAccountId?.decodeToLong() == userId }
                    .associate { it.skus[0] to it.purchaseToken }
                    .toList()
            if (purchasesList.isNotEmpty()) {
                incompletePurchases = InAppPurchasesUtils.getInCompletePurchases(
                    auditCoursesSku,
                    purchasesList
                )
                completePurchasesFlow(activity)
            } else {
                _completedUnfulfilledPurchase.postValue(true)
            }
        }
    }

    /**
     * To detect and handle courses which are purchased but still not Verified
     *
     * @param activity: context of the activity trigger to handle the incomplete purchases flow.
     * @return incompletePurchases after dropout the verified one.
     */
    private fun completePurchasesFlow(activity: Activity) {
        if (incompletePurchases.isNotEmpty()) {
            viewModelScope.launch {
                upgradeMode = UpgradeMode.SILENT
                purchaseToken = incompletePurchases[0].second
                addProductToBasket(activity, userId, incompletePurchases[0].first)
            }
        } else {
            _completedUnfulfilledPurchase.postValue(true)
        }
    }

    private fun markPurchaseComplete(activity: Activity) {
        incompletePurchases = incompletePurchases.drop(1).toMutableList()
        completePurchasesFlow(activity)
    }

    fun dispatchError(
        requestType: Int = 0,
        errorMessage: String? = null,
        throwable: Throwable? = null
    ) {
        var actualErrorMessage = errorMessage
        throwable?.let {
            if (errorMessage.isNullOrBlank()) {
                actualErrorMessage = it.message
            }
        }
        if (throwable != null && throwable is InAppPurchasesException) {
            _errorMessage.postValue(ErrorMessage(requestType = requestType, throwable = throwable))
        } else {
            _errorMessage.postValue(
                ErrorMessage(
                    requestType = requestType,
                    throwable = InAppPurchasesException(errorMessage = actualErrorMessage)
                )
            )
        }
    }

    private fun orderExecuted() {
        _productId = ""
        basketId = 0
        userId = 0
    }

    fun errorMessageShown() {
        _errorMessage.value = null
    }

    private fun startLoading() {
        _showLoader.postValue(true)
    }

    fun endLoading() {
        _showLoader.postValue(false)
    }

    fun showFullScreenLoader(show: Boolean) {
        _showFullscreenLoaderDialog.value = show
    }

    fun refreshCourseData(refresh: Boolean) {
        _refreshCourseData.postValue(refresh)
    }

    // To refrain the View Model from emitting further observable calls
    fun resetPurchase(complete: Boolean) {
        upgradeMode = UpgradeMode.NORMAL
        _isVerificationPending = true
        _purchaseFlowComplete.postValue(complete)
    }

    override fun onCleared() {
        billingProcessor.disconnect()
        super.onCleared()
    }

    enum class UpgradeMode {
        NORMAL,
        SILENT;

        fun isSilentMode() = this == SILENT

        fun isNormalMode() = this == NORMAL
    }
}
