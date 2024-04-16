package org.edx.mobile.repository

import org.edx.mobile.extenstion.toInAppPurchasesResult
import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.inapppurchases.InAppPurchasesAPI
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InAppPurchasesRepository(private var iapAPI: InAppPurchasesAPI) {

    fun addToBasket(productId: String, callback: NetworkResponseCallback<AddToBasketResponse>) {
        iapAPI.addToBasket(productId).enqueue(object : Callback<AddToBasketResponse> {
            override fun onResponse(
                call: Call<AddToBasketResponse>,
                response: Response<AddToBasketResponse>
            ) {
                response.toInAppPurchasesResult(callback)
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
                response.toInAppPurchasesResult(callback)
            }

            override fun onFailure(call: Call<CheckoutResponse>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }

    fun executeOrder(
        basketId: Long,
        purchaseToken: String,
        price: Double,
        currencyCode: String,
        callback: NetworkResponseCallback<ExecuteOrderResponse>
    ) {
        iapAPI.executeOrder(
            basketId = basketId,
            purchaseToken = purchaseToken,
            price = price,
            currencyCode = currencyCode,
        ).enqueue(object : Callback<ExecuteOrderResponse> {
            override fun onResponse(
                call: Call<ExecuteOrderResponse>,
                response: Response<ExecuteOrderResponse>
            ) {
                response.toInAppPurchasesResult(callback)
            }

            override fun onFailure(call: Call<ExecuteOrderResponse>, t: Throwable) {
                callback.onError(Result.Error(t))
            }
        })
    }
}
