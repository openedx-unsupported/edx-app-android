package org.edx.mobile.model.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ProfileModel(
    @JvmField
    @SerializedName("id")
    val id: Long,

    @JvmField
    @SerializedName("username")
    val username: String,

    @JvmField
    @SerializedName("email")
    var email: String?,

    @SerializedName("name")
    val name: String?,
) : Serializable {

    @JvmField
    var hasLimitedProfile = false
}
