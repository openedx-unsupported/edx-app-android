package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.edx.mobile.R
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import org.edx.mobile.repository.InAppPurchasesRepository
import org.edx.mobile.util.InAppPurchasesException
import javax.inject.Inject

@HiltViewModel
class InAppPurchasesViewModel @Inject constructor(
    private val repository: InAppPurchasesRepository
) : ViewModel() {

    private val _showLoader = MutableLiveData(false)
    val showLoader: LiveData<Boolean> = _showLoader

    private val _errorMessage = MutableLiveData<ErrorMessage?>()
    val errorMessage: LiveData<ErrorMessage?> = _errorMessage

    private val _checkoutResponse = MutableLiveData<CheckoutResponse>()
    val checkoutResponse: LiveData<CheckoutResponse> = _checkoutResponse

    private val _executeResponse = MutableLiveData<ExecuteOrderResponse>()
    val executeResponse: LiveData<ExecuteOrderResponse> = _executeResponse

    private val _showFullscreenLoaderDialog = MutableLiveData(false)
    val showFullscreenLoaderDialog: LiveData<Boolean> = _showFullscreenLoaderDialog

    private val _refreshCourseData = MutableLiveData(false)
    val refreshCourseData: LiveData<Boolean> = _refreshCourseData

    private val _purchaseFlowComplete = MutableLiveData(false)
    val purchaseFlowComplete: LiveData<Boolean> = _purchaseFlowComplete

    var upgradeMode = UpgradeMode.NORMAL

    private var _productId: String = ""
    val productId: String
        get() = _productId

    private var _isVerificationPending = true
    val isVerificationPending: Boolean
        get() = _isVerificationPending

    private var basketId: Long = 0
    private var purchaseToken: String = ""

    fun addProductToBasket(productId: String) {
        this._productId = productId
        startLoading()
        repository.addToBasket(
            productId = productId,
            callback = object : NetworkResponseCallback<AddToBasketResponse> {
                override fun onSuccess(result: Result.Success<AddToBasketResponse>) {
                    result.data?.let {
                        proceedCheckout(it.basketId)
                    } ?: endLoading()
                }

                override fun onError(error: Result.Error) {
                    endLoading()
                    setError(ErrorMessage.ADD_TO_BASKET_CODE, error.throwable)
                }
            })
    }

    fun proceedCheckout(basketId: Long) {
        this.basketId = basketId
        repository.proceedCheckout(
            basketId = basketId,
            callback = object : NetworkResponseCallback<CheckoutResponse> {
                override fun onSuccess(result: Result.Success<CheckoutResponse>) {
                    result.data?.let {
                        if (upgradeMode.isSilentMode()) executeOrder()
                        else _checkoutResponse.value = it
                    } ?: endLoading()
                }

                override fun onError(error: Result.Error) {
                    endLoading()
                    setError(ErrorMessage.CHECKOUT_CODE, error.throwable)
                }
            })
    }

    fun executeOrder() {
        _isVerificationPending = false
        repository.executeOrder(
            basketId = basketId,
            productId = productId,
            purchaseToken = purchaseToken,
            callback = object : NetworkResponseCallback<ExecuteOrderResponse> {
                override fun onSuccess(result: Result.Success<ExecuteOrderResponse>) {
                    result.data?.let {
                        orderExecuted()
                        if (upgradeMode.isSilentMode()) _executeResponse.postValue(it)
                        else refreshCourseData(true)
                    }
                    endLoading()
                }

                override fun onError(error: Result.Error) {
                    endLoading()
                    setError(ErrorMessage.EXECUTE_ORDER_CODE, error.throwable)
                }
            })
    }

    private fun orderExecuted() {
        _productId = ""
        basketId = 0
    }

    fun setError(errorCode: Int, throwable: Throwable) {
        _errorMessage.value = ErrorMessage(errorCode, throwable, getErrorMessage(throwable))
    }

    fun errorMessageShown() {
        _errorMessage.value = null
    }

    private fun getErrorMessage(throwable: Throwable) = if (throwable is InAppPurchasesException) {
        when (throwable.httpErrorCode) {
            400 -> when (throwable.errorCode) {
                ErrorMessage.ADD_TO_BASKET_CODE -> R.string.error_course_not_found
                ErrorMessage.CHECKOUT_CODE -> R.string.error_payment_not_processed
                ErrorMessage.EXECUTE_ORDER_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.general_error_message
            }
            403 -> when (throwable.errorCode) {
                ErrorMessage.EXECUTE_ORDER_CODE -> R.string.error_course_not_fullfilled
                else -> R.string.error_user_not_authenticated
            }
            406 -> R.string.error_course_already_paid
            else -> R.string.general_error_message
        }
    } else {
        R.string.general_error_message
    }

    private fun startLoading() {
        _showLoader.postValue(true)
    }

    fun endLoading() {
        _showLoader.postValue(false)
    }

    fun setPurchaseToken(purchaseToken: String) {
        this.purchaseToken = purchaseToken
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

    enum class UpgradeMode {
        NORMAL,
        SILENT;

        fun isSilentMode() = this == SILENT

        fun isNormalMode() = this == NORMAL
    }
}
