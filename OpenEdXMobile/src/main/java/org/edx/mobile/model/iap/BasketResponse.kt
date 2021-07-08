package org.edx.mobile.model.iap

import com.google.gson.annotations.SerializedName

data class BasketResponse(
    @SerializedName("success")
    var success: String,

    @SerializedName("basket_id")
    var basketId: String
)
