package org.edx.mobile.model.iap

import com.google.gson.annotations.SerializedName

data class CheckoutResponse(
    @SerializedName("processor_name")
    val processorName: String,

    @SerializedName("execution_url")
    val executionUrl: String
)
