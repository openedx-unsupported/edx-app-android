package org.edx.mobile.module.registration.model

import com.google.gson.annotations.SerializedName

data class ErrorMessage(
    @SerializedName("required")
    val required: String? = null,

    @SerializedName("min_length")
    val minLength: String? = null,

    @SerializedName("max_length")
    val maxLength: String? = null,
)
