package org.edx.mobile.viewModel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails
import com.android.billingclient.api.Purchase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.extenstion.decodeToLong
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.inapppurchases.BillingProcessor
import org.edx.mobile.inapppurchases.getPriceAmount
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.api.getAuditCourses
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import org.edx.mobile.model.iap.IAPFlowData
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics
import org.edx.mobile.repository.InAppPurchasesRepository
import org.edx.mobile.util.InAppPurchasesException
import org.edx.mobile.util.InAppPurchasesUtils
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import javax.inject.Inject

@HiltViewModel
class InAppPurchasesViewModel @Inject constructor(
    private val environment: IEdxEnvironment,
    private val billingProcessor: BillingProcessor,
    private val repository: InAppPurchasesRepository,
    private val iapAnalytics: InAppPurchasesAnalytics
) : ViewModel() {

    private val _productPrice = MutableLiveData<Event<OneTimePurchaseOfferDetails>>()
    val productPrice: LiveData<Event<OneTimePurchaseOfferDetails>> = _productPrice

    private val _launchPurchaseFlow = MutableLiveData<Event<Boolean>>()
    val launchPurchaseFlow: LiveData<Event<Boolean>> = _launchPurchaseFlow

    private val _productPurchased = MutableLiveData<Event<IAPFlowData>>()
    val productPurchased: LiveData<Event<IAPFlowData>> = _productPurchased

    private val _refreshCourseData = MutableLiveData<Event<IAPFlowData>>()
    val refreshCourseData: LiveData<Event<IAPFlowData>> = _refreshCourseData

    private val _fakeUnfulfilledCompletion = MutableLiveData<Event<Boolean>>()
    val fakeUnfulfilledCompletion: LiveData<Event<Boolean>> = _fakeUnfulfilledCompletion

    private val _showLoader = MutableLiveData<Event<Boolean>>()
    val showLoader: LiveData<Event<Boolean>> = _showLoader

    private val _errorMessage = MutableLiveData<Event<ErrorMessage>>()
    val errorMessage: LiveData<Event<ErrorMessage>> = _errorMessage

    var iapFlowData = IAPFlowData()
    private var incompletePurchases: MutableList<IAPFlowData> = arrayListOf()

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
            if (purchase.products.first() == iapFlowData.productId) {
                iapFlowData.purchaseToken = purchase.purchaseToken
                _productPurchased.postEvent(iapFlowData)
                iapAnalytics.trackIAPEvent(eventName = Analytics.Events.IAP_PAYMENT_TIME)
                iapAnalytics.initUnlockContentTime()
            }
        }
    }

    init {
        billingProcessor.setUpBillingFlowListeners(listener)
    }

    fun initializeProductPrice(courseSku: String?) {
        iapAnalytics.initPriceTime()
        if (courseSku == null) {
            dispatchError(requestType = ErrorMessage.PRICE_CODE)
            return
        }

        viewModelScope.launch {
            val response = billingProcessor.querySyncDetails(courseSku)
            val productDetail = response.productDetailsList?.firstOrNull()

            if (productDetail?.productId == courseSku) {
                productDetail.oneTimePurchaseOfferDetails?.let {
                    _productPrice.postEvent(it)
                    iapAnalytics.setPrice(it.formattedPrice)
                    iapAnalytics.trackIAPEvent(Analytics.Events.IAP_LOAD_PRICE_TIME)
                } ?: dispatchError(
                    requestType = ErrorMessage.PRICE_CODE,
                    throwable = InAppPurchasesException(
                        httpErrorCode = response.billingResult.responseCode,
                        errorMessage = response.billingResult.debugMessage,
                    )
                )
            }
        }
    }

    fun startPurchaseFlow(productId: String, price: Double, currencyCode: String) {
        iapFlowData.productId = productId
        iapFlowData.price = price
        iapFlowData.currencyCode = currencyCode
        iapFlowData.flowType = IAPFlowData.IAPFlowType.USER_INITIATED
        iapFlowData.isVerificationPending = true
        addProductToBasket()
    }

    private fun addProductToBasket() {
        startLoading()
        repository.addToBasket(
            productId = iapFlowData.productId,
            callback = object : NetworkResponseCallback<AddToBasketResponse> {
                override fun onSuccess(result: Result.Success<AddToBasketResponse>) {
                    result.data?.let {
                        iapFlowData.basketId = it.basketId
                        proceedCheckout()
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

    fun proceedCheckout() {
        repository.proceedCheckout(
            basketId = iapFlowData.basketId,
            callback = object : NetworkResponseCallback<CheckoutResponse> {
                override fun onSuccess(result: Result.Success<CheckoutResponse>) {
                    result.data?.let {
                        if (iapFlowData.flowType.isSilentMode()) {
                            executeOrder(iapFlowData)
                        } else {
                            iapAnalytics.initPaymentTime()
                            _launchPurchaseFlow.postEvent(true)
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

    fun purchaseItem(activity: Activity, userId: Long, courseSku: String?) {
        viewModelScope.launch {
            courseSku?.let {
                billingProcessor.purchaseItem(activity, courseSku, userId)
            }
        }
    }

    fun executeOrder(iapFlowData: IAPFlowData? = this.iapFlowData) {
        iapFlowData?.let { iapData ->
            iapData.isVerificationPending = false
            this.iapFlowData = iapData
            repository.executeOrder(
                basketId = iapData.basketId,
                productId = iapData.productId,
                purchaseToken = iapData.purchaseToken,
                price = iapData.price,
                currencyCode = iapData.currencyCode,
                callback = object : NetworkResponseCallback<ExecuteOrderResponse> {
                    override fun onSuccess(result: Result.Success<ExecuteOrderResponse>) {
                        result.data?.let {
                            iapData.isVerificationPending = false
                            if (iapFlowData.flowType.isSilentMode()) {
                                markPurchaseComplete(iapData)
                            } else {
                                _refreshCourseData.postEvent(iapData)
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
    }

    /**
     * To detect and handle courses which are purchased but still not Verified
     *
     * @param userId          id of the user to detect un full filled purchases and process.
     * @param enrolledCourses user enrolled courses in the platform.
     * @param flowType        [IAPFlowData.IAPFlowType] of payment
     * @param screenName      name of the screen from where the flow is initiated
     */
    fun detectUnfulfilledPurchase(
        userId: Long,
        enrolledCourses: List<EnrolledCoursesResponse>,
        flowType: IAPFlowData.IAPFlowType,
        screenName: String
    ) {
        val auditCourses = enrolledCourses.getAuditCourses()
        if (auditCourses.isEmpty()) {
            _fakeUnfulfilledCompletion.postEvent(true)
            return
        }
        viewModelScope.launch {
            val purchases = billingProcessor.queryPurchases()
            environment.featuresPrefs.canAutoCheckUnfulfilledPurchase = false
            if (purchases.isEmpty()) {
                _fakeUnfulfilledCompletion.postEvent(true)
                return@launch
            }

            val userPurchases = purchases.filter {
                it.accountIdentifiers?.obfuscatedAccountId?.decodeToLong() == userId
            }
            if (userPurchases.isNotEmpty()) {
                incompletePurchases =
                    InAppPurchasesUtils.getInCompletePurchases(
                        auditCourses,
                        userPurchases,
                        flowType,
                        screenName
                    )
                if (incompletePurchases.isEmpty()) {
                    _fakeUnfulfilledCompletion.postEvent(true)
                } else {
                    startUnfulfilledVerification()
                }
            } else {
                _fakeUnfulfilledCompletion.postEvent(true)
            }
        }
    }

    /**
     * Method to start the process to verify the un full filled courses skus
     */
    private fun startUnfulfilledVerification() {
        iapFlowData.clear()
        iapFlowData = incompletePurchases[0]
        // will perform verification as part of unfulfilled flow
        iapFlowData.isVerificationPending = false
        iapAnalytics.initCourseValues(
            courseId = iapFlowData.courseId,
            isSelfPaced = iapFlowData.isCourseSelfPaced,
            flowType = iapFlowData.flowType.value(),
            screenName = iapFlowData.screenName
        )
        iapAnalytics.trackIAPEvent(Analytics.Events.IAP_UNFULFILLED_PURCHASE_INITIATED)

        //Start the purchase flow
        viewModelScope.launch {
            val response = billingProcessor.querySyncDetails(iapFlowData.productId)
            val productDetail = response.productDetailsList?.firstOrNull()
            if (productDetail?.productId == iapFlowData.productId) {
                productDetail.oneTimePurchaseOfferDetails?.let {
                    iapFlowData.currencyCode = it.priceCurrencyCode
                    iapFlowData.price = it.getPriceAmount()
                    addProductToBasket()
                }
            }
        }
    }

    private fun markPurchaseComplete(iapFlowData: IAPFlowData) {
        incompletePurchases = incompletePurchases.drop(1).toMutableList()
        if (incompletePurchases.isEmpty()) {
            _refreshCourseData.postEvent(iapFlowData)
        } else {
            startUnfulfilledVerification()
        }
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
            _errorMessage.postEvent(ErrorMessage(requestType = requestType, throwable = throwable))
        } else {
            _errorMessage.postEvent(
                ErrorMessage(
                    requestType = requestType,
                    throwable = InAppPurchasesException(errorMessage = actualErrorMessage)
                )
            )
        }
    }

    private fun startLoading() {
        _showLoader.postEvent(true)
    }

    fun endLoading() {
        _showLoader.postEvent(false)
    }
}
