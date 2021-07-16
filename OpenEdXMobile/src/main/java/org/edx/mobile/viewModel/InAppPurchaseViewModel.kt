package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.edx.mobile.http.callback.Callback
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.iap.BasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.PaymentExecutionResponse
import org.edx.mobile.repositorie.InAppPaymentsRepository

class InAppPurchaseViewModel constructor(var inAppPaymentsRepository: InAppPaymentsRepository) :
    ViewModel() {

    private val _basketResponse = MutableLiveData<BasketResponse>()
    private val _checkOutResponse = MutableLiveData<CheckoutResponse>()
    private val _paymentExecutionResponse = MutableLiveData<PaymentExecutionResponse>()

    val basketResponse: LiveData<BasketResponse>
        get() = _basketResponse

    val checkoutResponse: LiveData<CheckoutResponse>
        get() = _checkOutResponse

    val paymentExecution: LiveData<PaymentExecutionResponse>
        get() = _paymentExecutionResponse

    fun addToBasket(sku: String) {
        inAppPaymentsRepository.addToBasket(sku, object : Callback<BasketResponse>() {
            override fun onResponse(responseBody: BasketResponse) {
                _basketResponse.postValue(responseBody)
            }

            override fun onFailure(error: Throwable) {
                super.onFailure(error)
                logger.error(error)
            }
        })
    }

    fun checkout(url: String, basketId: String) {
        inAppPaymentsRepository.checkout(url, basketId, object : Callback<CheckoutResponse>() {
            override fun onResponse(responseBody: CheckoutResponse) {
                responseBody.basketId = basketId
                _checkOutResponse.postValue(responseBody)
            }

            override fun onFailure(error: Throwable) {
                super.onFailure(error)
                logger.error(error)
            }
        })
    }

    fun paymentExecution(executionURL: String, paymentMap: HashMap<String, String>) {
        inAppPaymentsRepository.paymentExecution(executionURL,
            paymentMap,
            object : Callback<PaymentExecutionResponse>() {
                override fun onResponse(paymentExecutionResponse: PaymentExecutionResponse) {
                    _paymentExecutionResponse.postValue(paymentExecutionResponse)
                }

                override fun onFailure(error: Throwable) {
                    super.onFailure(error)
                    logger.error(error)
                }
            })
    }

    companion object {
        private val TAG = InAppPurchaseViewModel::class.java.simpleName
        private val logger = Logger(TAG)
    }
}