package org.edx.mobile.model

import com.google.gson.annotations.SerializedName

data class CourseDatesCalendarSync(
        @SerializedName("DISABLED_FOR_VERSIONS") val disabledVersions: ArrayList<String> = arrayListOf(),
        @SerializedName("SELF_PACED_ENABLED") val isSelfPlacedEnable: Boolean = false,
        @SerializedName("INSTRUCTOR_PACED_ENABLED") val isInstructorPlacedEnable: Boolean = false,
        @SerializedName("DEEP_LINKS_ENABLED") val isDeepLinkEnabled: Boolean = false
)
