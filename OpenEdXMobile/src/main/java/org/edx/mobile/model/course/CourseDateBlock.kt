package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName
import org.edx.mobile.util.CourseDateType
import org.edx.mobile.util.DateUtil

data class CourseDateBlock(
        @SerializedName("complete") var complete: Boolean = false,
        @SerializedName("date") val date: String = "",
        @SerializedName("assignment_type") val assignmentType: String? = "",
        @SerializedName("date_type") var dateType: String? = "",
        @SerializedName("description") val description: String = "",
        @SerializedName("learner_has_access") var learnerHasAccess: Boolean = false,
        @SerializedName("link") val link: String = "",
        @SerializedName("link_text") val linkText: String = "",
        @SerializedName("title") val title: String = "",
        // Local property to assign a badge to a date block according to desired result
        var dateBlockBadge: CourseDateType = CourseDateType.BLANK

) {
    companion object {
        fun getTodayDateBlock() = CourseDateBlock(date = DateUtil.getCurrentTimeStamp(), dateType = DateTypes.TODAY_DATE)
    }

    fun isToday(): Boolean = (DateUtil.isDateToday(date) || isDateTypeToday())

    fun isDateTypeToday(): Boolean = dateType.equals(DateTypes.TODAY_DATE)

    fun getFormattedDate(): String = DateUtil.formatCourseDate(date)

    fun getSimpleDateTime(): String = DateUtil.convertToSimpleDate(date)

    fun isDatePassed(): Boolean = DateUtil.isPastDate(date)

    fun isAssignment(): Boolean = dateType.equals(DateTypes.ASSIGNMENT_DUE_DATE)

    fun isLearnerAssignment(): Boolean = learnerHasAccess && isAssignment()

    fun showLink(): Boolean = link.isNotBlank() && isLearnerAssignment()

    object DateTypes {
        const val TODAY_DATE = "todays-date"
        const val COURSE_START_DATE = "course-start-date"
        const val COURSE_END_DATE = "course-end-date"
        const val COURSE_EXPIRED_DATE = "course-expired-date"
        const val ASSIGNMENT_DUE_DATE = "assignment-due-date"
        const val CERTIFICATE_AVAILABLE_DATE = "certificate-available-date"
        const val VERIFIED_UPGRADE_DEADLINE = "verified-upgrade-deadline"
        const val VERIFICATION_DEADLINE_DATE = "verification-deadline-date"
    }
}
