package org.edx.mobile.model.iap

import com.google.gson.annotations.SerializedName

data class ExecuteOrderResponse(
    @SerializedName("order_data") val orderData: OrderData
)

data class OrderData(
    @SerializedName("billing_address") val billingAddress: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("date_placed") val datePlaced: String,
    @SerializedName("discount") val discount: String,
    @SerializedName("number") val number: String,
    @SerializedName("payment_processor") val paymentProcessor: String,
    @SerializedName("status") val status: String,
    @SerializedName("total_excl_tax") val totalExclTax: String,
    @SerializedName("user") val user: User
)

data class User(
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String
)
