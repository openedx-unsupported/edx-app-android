package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

data class CourseStatus(
        @SerializedName("celebrations")
        var celebrationStatus: CelebrationStatus
)

data class CelebrationStatus(
        @SerializedName("first_section")
        var firstSection: Boolean = false
)
