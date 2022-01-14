package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import org.edx.mobile.repositorie.InAppPurchasesRepository
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class InAppPurchasesViewModel @Inject constructor(
    private val repository: InAppPurchasesRepository
) : ViewModel() {

    private val _showLoader = MutableLiveData<Boolean>()
    val showLoader: LiveData<Boolean> = _showLoader

    private val _errorMessage = MutableLiveData<ErrorMessage>()
    val errorMessage: LiveData<ErrorMessage> = _errorMessage

    private val _checkoutResponse = MutableLiveData<CheckoutResponse>()
    val checkoutResponse: LiveData<CheckoutResponse> = _checkoutResponse

    private val _executeOrderResponse = MutableLiveData<ExecuteOrderResponse>()
    val executeOrderResponse: LiveData<ExecuteOrderResponse> = _executeOrderResponse

    private var productId: String = ""
    private var basketId: Long = 0

    fun getProductId() = productId

    fun startLoading() {
        _showLoader.value = true
    }

    fun endLoading() {
        _showLoader.postValue(false)
    }

    fun addProductToBasket(productId: String) {
        this.productId = productId
        startLoading()
        repository.addToBasket(
            productId = productId,
            callback = object : NetworkResponseCallback<AddToBasketResponse> {
                override fun onSuccess(result: Result.Success<AddToBasketResponse>) {
                    if (result.isSuccessful && result.data != null) {
                        proceedCheckout(result.data.basketId)
                    } else {
                        setError(ErrorMessage.ADD_TO_BASKET_CODE, result.code, result.message)
                        endLoading()
                    }
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
                    if (result.isSuccessful && result.data != null) {
                        _checkoutResponse.value = result.data
                    } else {
                        setError(ErrorMessage.CHECKOUT_CODE, result.code, result.message)
                        endLoading()
                    }
                }

                override fun onError(error: Result.Error) {
                    endLoading()
                    setError(ErrorMessage.CHECKOUT_CODE, error.throwable)
                }
            })
    }

    fun executeOrder(purchaseToken: String) {
        repository.executeOrder(
            basketId = basketId,
            productId = productId,
            purchaseToken = purchaseToken,
            callback = object : NetworkResponseCallback<ExecuteOrderResponse> {
                override fun onSuccess(result: Result.Success<ExecuteOrderResponse>) {
                    if (result.isSuccessful && result.data != null) {
                        _executeOrderResponse.value = result.data
                        orderExecuted()
                    } else {
                        setError(ErrorMessage.EXECUTE_ORDER_CODE, result.code, result.message)
                    }
                    endLoading()
                }

                override fun onError(error: Result.Error) {
                    endLoading()
                    setError(ErrorMessage.EXECUTE_ORDER_CODE, error.throwable)
                }
            })
    }

    fun setError(errorCode: Int, httpStatusCode: Int, msg: String) {
        setError(
            errorCode,
            HttpStatusException(
                Response.error<Any>(
                    httpStatusCode,
                    ResponseBody.create("text/plain".toMediaTypeOrNull(), msg)
                )
            )
        )
    }

    fun setError(errorCode: Int, throwable: Throwable) {
        _errorMessage.value = ErrorMessage(errorCode, throwable)
    }

    fun errorMessageShown() {
        _errorMessage.value = null
    }

    private fun orderExecuted() {
        this.productId = ""
        this.basketId = 0
    }
}
