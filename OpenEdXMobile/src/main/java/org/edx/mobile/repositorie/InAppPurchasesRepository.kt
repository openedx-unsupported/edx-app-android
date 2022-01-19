package org.edx.mobile.repositorie

import org.edx.mobile.base.MainApplication
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.inapppurchases.InAppPurchasesAPI
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import roboguice.RoboGuice

class InAppPurchasesRepository {

    private var iapAPI: InAppPurchasesAPI =
        RoboGuice.getInjector(MainApplication.application)
            .getInstance(InAppPurchasesAPI::class.java)

    companion object {
        private var instance: InAppPurchasesRepository? = null
        fun getInstance(): InAppPurchasesRepository =
            instance ?: synchronized(this) {
                instance ?: InAppPurchasesRepository().also { instance = it }
            }
    }

    fun addToBasket(productId: String, callback: NetworkResponseCallback<AddToBasketResponse>) {
        iapAPI.addToBasket(productId).enqueue(object : Callback<AddToBasketResponse> {
            override fun onResponse(
                call: Call<AddToBasketResponse>,
                response: Response<AddToBasketResponse>
            ) {
                callback.onSuccess(
                    Result.Success<AddToBasketResponse>(
                        isSuccessful = response.isSuccessful,
                        data = response.body(),
                        code = response.code(),
                        message = response.message()
                    )
                )
            }

            override fun onFailure(call: Call<AddToBasketResponse>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }

    fun proceedCheckout(basketId: Long, callback: NetworkResponseCallback<CheckoutResponse>) {
        iapAPI.proceedCheckout(basketId = basketId).enqueue(object : Callback<CheckoutResponse> {
            override fun onResponse(
                call: Call<CheckoutResponse>,
                response: Response<CheckoutResponse>
            ) {
                callback.onSuccess(
                    Result.Success<CheckoutResponse>(
                        isSuccessful = response.isSuccessful,
                        data = response.body(),
                        code = response.code(),
                        message = response.message()
                    )
                )
            }

            override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }

    fun executeOrder(
        basketId: Long,
        productId: String,
        purchaseToken: String,
        callback: NetworkResponseCallback<ExecuteOrderResponse>
    ) {
        iapAPI.executeOrder(
            basketId = basketId,
            productId = productId,
            purchaseToken = purchaseToken
        ).enqueue(object : Callback<ExecuteOrderResponse> {
            override fun onResponse(
                call: Call<ExecuteOrderResponse>,
                response: Response<ExecuteOrderResponse>
            ) {
                callback.onSuccess(
                    Result.Success<ExecuteOrderResponse>(
                        isSuccessful = response.isSuccessful,
                        data = response.body(),
                        code = response.code(),
                        message = response.message()
                    )
                )
            }

            override fun onFailure(call: Call<ExecuteOrderResponse>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }
}
