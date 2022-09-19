package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

class EnrollBody(courseId: String, emailOptIn: Boolean) {

    @SerializedName("course_details")
    private val courseDetails = CourseDetails(courseId, emailOptIn)

    private inner class CourseDetails(
        @SerializedName("course_id")
        private val courseId: String,

        @SerializedName("email_opt_in")
        private val emailOptIn: Boolean
    )
}
