package org.edx.mobile.model.course

/**
 * Course Enrollment modes
 */
enum class EnrollmentMode(private val mode: String) {
    AUDIT("audit"), VERIFIED("verified"), HONOR("honor"),
    NO_ID_PROFESSIONAL("no-id-professional"), PROFESSIONAL("professional"),
    CREDIT("credit"), MASTERS("masters"), NONE("none");

    override fun toString(): String {
        return mode
    }
}
