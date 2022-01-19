package org.edx.mobile.inapppurchases

import com.google.inject.Inject
import com.google.inject.Singleton
import org.edx.mobile.http.constants.ApiConstants
import org.edx.mobile.model.iap.AddToBasketResponse
import org.edx.mobile.model.iap.CheckoutResponse
import org.edx.mobile.model.iap.ExecuteOrderResponse
import retrofit2.Call

@Singleton
class InAppPurchasesAPI @Inject constructor(private val iapService: InAppPurchasesService) {

    fun addToBasket(productId: String): Call<AddToBasketResponse> {
        return iapService.addToBasket(productId)
    }

    fun proceedCheckout(basketId: Long): Call<CheckoutResponse> {
        return iapService.proceedCheckout(
            basketId = basketId,
            paymentProcessor = ApiConstants.PAYMENT_PROCESSOR
        )
    }

    fun executeOrder(
        basketId: Long,
        productId: String,
        purchaseToken: String
    ): Call<ExecuteOrderResponse> {
        return iapService.executeOrder(
            basketId = basketId,
            productId = productId,
            paymentProcessor = ApiConstants.PAYMENT_PROCESSOR,
            purchaseToken = purchaseToken
        )
    }
}
