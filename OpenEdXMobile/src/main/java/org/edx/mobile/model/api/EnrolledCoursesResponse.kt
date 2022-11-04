package org.edx.mobile.model.api

import com.google.gson.annotations.SerializedName
import org.edx.mobile.interfaces.SectionItemInterface
import org.edx.mobile.model.course.EnrollmentMode

data class EnrolledCoursesResponse(

    @SerializedName("mode")
    var mode: String,

    @SerializedName("audit_access_expires")
    val auditAccessExpires: String,

    @SerializedName("is_active")
    val isActive: Boolean = false,

    @SerializedName("course")
    val course: CourseEntry,

    @SerializedName("certificate")
    private val certificate: CertificateModel?,

    @SerializedName("course_modes")
    private val courseModes: List<CourseMode>?,
) : SectionItemInterface {

    var isDiscussionBlackedOut: Boolean = false

    val courseId: String
        get() = course.id

    val certificateURL: String?
        get() = certificate?.certificateURL

    val isCertificateEarned: Boolean
        get() = certificateURL.isNullOrEmpty().not()

    val courseSku: String?
        get() = courseModes?.firstOrNull { item ->
            EnrollmentMode.VERIFIED.name.equals(item.slug, ignoreCase = true)
        }?.androidSku.takeUnless { it.isNullOrEmpty() }

    val isAuditMode: Boolean
        get() = EnrollmentMode.AUDIT.toString().equals(mode, ignoreCase = true)

    override fun isChapter(): Boolean {
        return false
    }

    override fun isSection(): Boolean {
        return false
    }

    override fun toString(): String {
        return course.name
    }

    override fun isCourse(): Boolean {
        return true
    }

    override fun isVideo(): Boolean {
        return false
    }

    override fun isDownload(): Boolean {
        return false
    }
}
