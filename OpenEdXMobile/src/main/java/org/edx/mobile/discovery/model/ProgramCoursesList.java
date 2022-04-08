package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProgramCoursesList {
    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("title")
    @Expose
    private String title;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @SerializedName("type")
    @Expose
    private String type;

    public boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }

    private boolean enrolled;

    public List<CourseRuns> getCourseRuns() {
        return courseRuns;
    }

    public void setCourseRuns(List<CourseRuns> courseRuns) {
        this.courseRuns = courseRuns;
    }

    @SerializedName("course_runs")
    @Expose
    private List<CourseRuns>courseRuns;

}
