package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

data class CourseDatesBannerInfo(
        @SerializedName("missed_deadlines") val missedDeadlines: Boolean = false,
        @SerializedName("missed_gated_content") val missedGatedContent: Boolean = false,
        @SerializedName("verified_upgrade_link") val verifiedUpgradeLink: String = "",
        @SerializedName("content_type_gating_enabled") val contentTypeGatingEnabled: Boolean = false
) {
    fun getCourseBannerType(): CourseBannerType = when {
        upgradeToGraded() -> CourseBannerType.UPGRADE_TO_GRADED
        upgradeToReset() -> CourseBannerType.UPGRADE_TO_RESET
        resetDates() -> CourseBannerType.RESET_DATES
        showBannerInfo() -> CourseBannerType.INFO_BANNER
        else -> CourseBannerType.BLANK
    }

    private fun showBannerInfo(): Boolean = missedDeadlines.not()

    private fun upgradeToGraded(): Boolean = contentTypeGatingEnabled && missedDeadlines.not()

    private fun upgradeToReset(): Boolean = upgradeToGraded().not() && missedDeadlines && missedGatedContent

    private fun resetDates(): Boolean = upgradeToGraded().not() && missedDeadlines && missedGatedContent.not()
}

enum class CourseBannerType {
    BLANK, INFO_BANNER, UPGRADE_TO_GRADED, UPGRADE_TO_RESET, RESET_DATES;
}
