package org.edx.mobile.module.registration.model

import com.google.gson.annotations.SerializedName

data class RegistrationOption(
    @SerializedName("default")
    var isDefaultValue: Boolean = false,

    @SerializedName("name")
    var name: String = "",

    @SerializedName("value")
    val value: String = "",
) {
    override fun toString(): String {
        return name
    }
}
