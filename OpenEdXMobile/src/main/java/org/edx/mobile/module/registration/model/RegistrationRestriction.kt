package org.edx.mobile.module.registration.model

import com.google.gson.annotations.SerializedName

data class RegistrationRestriction(
    @SerializedName("min_length")
    val minLength: Int = -1,

    @SerializedName("max_length")
    val maxLength: Int = -1,
)
