package org.edx.mobile.model.iap

import com.google.gson.annotations.SerializedName

data class PaymentExecutionResponse(
    @SerializedName("purchaseToken")
    val purchaseToken: String,

    @SerializedName("basket_id")
    val basketId: String,

    @SerializedName("productId")
    val productId: String,

    @SerializedName("transactionId")
    val transactionId: String
)
