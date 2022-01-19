package org.edx.mobile.inapppurchases

import com.google.inject.Inject
import org.edx.mobile.http.provider.RetrofitProvider
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface InAppPurchasesService {

    class Provider : com.google.inject.Provider<InAppPurchasesService> {
        @Inject
        var retrofitProvider: RetrofitProvider? = null
        override fun get(): InAppPurchasesService? {
            return retrofitProvider?.iapAuth?.create(InAppPurchasesService::class.java)
        }
    }

    @GET("/api/iap/v1/basket/add/")
    fun addToBasket(@Query("sku") productId: String): Call<AddToBasketResponse>

    @FormUrlEncoded
    @POST("/api/v2/checkout/")
    fun proceedCheckout(
        @Field("basket_id") basketId: Long,
        @Field("payment_processor") paymentProcessor: String
    ): Call<CheckoutResponse>

    @FormUrlEncoded
    @POST("/api/iap/v1/execute/")
    fun executeOrder(
        @Field("basket_id") basketId: Long,
        @Field("productId") productId: String,
        @Field("payment_processor") paymentProcessor: String,
        @Field("purchaseToken") purchaseToken: String
    ): Call<ExecuteOrderResponse>
}
