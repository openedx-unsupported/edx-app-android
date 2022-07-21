package org.edx.mobile.model.api;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.course.EnrollmentMode;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class EnrolledCoursesResponse implements SectionItemInterface {

    private String auditAccessExpires;
    private String created;
    private String mode;
    private boolean is_active;
    private CourseEntry course;
    private boolean isDiscussionBlackedOut = false;

    private CertificateModel certificate;

    @SerializedName("course_modes")
    private ArrayList<CourseMode> courseModes;

    // derived fields (doesn't come in server response)
    public int videoCount;
    public long size;

    public String getAuditAccessExpires() {
        return auditAccessExpires;
    }

    public void setAuditAccessExpires(String auditAccessExpires) {
        this.auditAccessExpires = auditAccessExpires;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public CourseEntry getCourse() {
        return course;
    }

    public void setCourse(CourseEntry course) {
        this.course = course;
    }

    public String getCourseId() {
        return course.getId();
    }

    @Override
    public boolean isChapter() {
        return false;
    }

    @Override
    public boolean isSection() {
        return false;
    }

    @Override
    public String toString() {
        return course.getName();
    }

    @Override
    public boolean isCourse() {
        return true;
    }

    @Override
    public boolean isVideo() {
        return false;
    }

    @Override
    public boolean isDownload() {
        return false;
    }

    public String getVideoCountReadable() {
        return String.format("%d %s", videoCount, (videoCount == 1 ? "Video" : "Videos"));
    }

    public String getCertificateURL() {
        return this.certificate == null ? null : this.certificate.certificateURL;
    }

    public boolean isCertificateEarned() {
        return this.certificate != null && !TextUtils.isEmpty(this.certificate.certificateURL);

    }

    public boolean isDiscussionBlackedOut() {
        return isDiscussionBlackedOut;
    }

    public void setDiscussionBlackedOut(boolean discussionBlackedOut) {
        isDiscussionBlackedOut = discussionBlackedOut;
    }

    public String getProductSku() {
        if (courseModes == null || courseModes.size() == 0) {
            return null;
        } else {
            for (CourseMode courseMode : courseModes) {
                if (EnrollmentMode.VERIFIED.name().equalsIgnoreCase(courseMode.getSlug())) {
                    return TextUtils.isEmpty(courseMode.getAndroidSku()) ? null : courseMode.getAndroidSku();
                }
            }
        }
        return null;
    }
}
