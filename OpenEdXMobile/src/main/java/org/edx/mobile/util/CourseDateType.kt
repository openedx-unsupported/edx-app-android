package org.edx.mobile.util

/**
 * This enum defines the Date type of Course Dates
 */
enum class CourseDateType {
    TODAY, BLANK, VERIFIED_ONLY, COMPLETED, PAST_DUE, DUE_NEXT, NOT_YET_RELEASED,
    COURSE_EXPIRED_DATE;

    fun getTitle(): String {
        return when (this) {
            TODAY -> "Today"
            VERIFIED_ONLY -> "Verified Only"
            COMPLETED -> "Completed"
            PAST_DUE -> "Past Due"
            DUE_NEXT -> "Due Next"
            NOT_YET_RELEASED -> "Not Yet Released"
            else -> ""
        }
    }
}
