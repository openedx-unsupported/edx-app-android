package org.edx.mobile.module.registration.model

import com.google.gson.annotations.SerializedName

data class RegistrationDescription(
    @SerializedName("submit_url")
    val endpoint: String? = null,

    @SerializedName("method")
    val method: String? = null,

    @SerializedName("fields")
    var fields: List<RegistrationFormField> = listOf(),
)
