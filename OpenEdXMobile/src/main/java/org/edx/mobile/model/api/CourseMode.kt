package org.edx.mobile.model.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CourseMode(
    @SerializedName("slug")
    val slug: String?,

    @SerializedName("sku")
    val sku: String?,

    @SerializedName("android_sku")
    val androidSku: String?,
) : Serializable
