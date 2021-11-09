package org.edx.mobile.model.api

import com.google.gson.annotations.SerializedName

data class UnacknowledgedNoticeResponse(
    @SerializedName("results")
    var results: ArrayList<String> = arrayListOf()
)
