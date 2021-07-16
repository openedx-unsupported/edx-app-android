package org.edx.mobile.model.iap

import com.google.gson.annotations.SerializedName

data class CheckoutResponse(

    @SerializedName("payment_form_data")
    val paymentFormData: Object,

    @SerializedName("payment_page_url")
    val paymentPageUrl: String,

    @SerializedName("payment_processor")
    val paymentProcessor: String
) {
    lateinit var basketId: String
}
