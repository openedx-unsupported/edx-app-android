package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CourseRuns {
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @SerializedName("key")
    @Expose
    private String key;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @SerializedName("uuid")
    @Expose
    private String uuid;

    @SerializedName("title")
    @Expose
    private String title;

    public String getCourse_status() {
        return course_status;
    }

    public void setCourse_status(String course_status) {
        this.course_status = course_status;
    }

    private String course_status;
}
