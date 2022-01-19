package org.edx.mobile.model.iap

import com.google.gson.annotations.SerializedName

data class AddToBasketResponse(
    @SerializedName("success") val success: String,
    @SerializedName("basket_id") val basketId: Long
)
