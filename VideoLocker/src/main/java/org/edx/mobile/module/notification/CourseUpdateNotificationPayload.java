package org.edx.mobile.module.notification;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Text;

import java.util.List;

/**
{
        "title": "Course Announcement",
        "loc-args": [
        "VAL video"
        ],
        "action-loc-key": "VIEW_BUTTON",
        "loc-key": "COURSE_ANNOUNCEMENT_NOTIFICATION_BODY",
        "action": "course.announcement",
        "title-loc-key": "COURSE_ANNOUNCEMENT_NOTIFICATION_TITLE",
        "title-loc-args": []
        }
 **/
public class CourseUpdateNotificationPayload {

    private @SerializedName("push_hash") String pushHash;
    private @SerializedName("title") String title;
    private @SerializedName("loc-args") List<String> locArgs;
    private @SerializedName("action-loc-key") String actionLocKey;
    private @SerializedName("loc-key") String localKey;
    private @SerializedName("action") String action;
    private @SerializedName("title-loc-key") String titleLocKey;
    private @SerializedName("title-loc-args") List<String> titleLocArgs;
    private @SerializedName("course-id") String courseId;
    private @SerializedName("alert") String alert;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLocArgs() {
        return locArgs;
    }

    public void setLocArgs(List<String> locArgs) {
        this.locArgs = locArgs;
    }

    public String getActionLocKey() {
        return actionLocKey;
    }

    public void setActionLocKey(String actionLocKey) {
        this.actionLocKey = actionLocKey;
    }

    public String getLocalKey() {
        return localKey;
    }

    public void setLocalKey(String localKey) {
        this.localKey = localKey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTitleLocKey() {
        return titleLocKey;
    }

    public void setTitleLocKey(String titleLocKey) {
        this.titleLocKey = titleLocKey;
    }

    public List<String> getTitleLocArgs() {
        return titleLocArgs;
    }

    public void setTitleLocArgs(List<String> titleLocArgs) {
        this.titleLocArgs = titleLocArgs;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getPushHash() {
        return pushHash;
    }

    public void setPushHash(String pushHash) {
        this.pushHash = pushHash;
    }

    public boolean isValid(){
        return !TextUtils.isEmpty(courseId);
    }


}
