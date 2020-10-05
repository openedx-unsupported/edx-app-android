package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

data class ResetCourseDates(
        @SerializedName("message") val message: String = "",
        @SerializedName("body") val body: String = "",
        @SerializedName("header") val header: String = "",
        @SerializedName("link") val link: String = "",
        @SerializedName("link_text") val linkText: String = ""
)
