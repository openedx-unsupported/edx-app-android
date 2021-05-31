package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

data class SpecialExamInfo(
        @SerializedName("short_description") val shortDescription: String = "",
        @SerializedName("suggested_icon") val suggestedIcon: String = "",
        @SerializedName("in_completed_state") val inCompletedState: Boolean = false
)
