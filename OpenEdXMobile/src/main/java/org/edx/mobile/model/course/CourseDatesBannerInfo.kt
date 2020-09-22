package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

data class CourseDatesBannerInfo(
        @SerializedName("missed_deadlines") val missedDeadlines: Boolean = false,
        @SerializedName("missed_gated_content") val missedGatedContent: Boolean = false,
        @SerializedName("verified_upgrade_link") val verifiedUpgradeLink: String = "",
        @SerializedName("content_type_gating_enabled") val contentTypeGatingEnabled: Boolean = false
)
