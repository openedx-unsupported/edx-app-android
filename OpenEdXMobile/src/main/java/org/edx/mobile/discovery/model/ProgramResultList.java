package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProgramResultList {
    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ProgramCoursesList> getCourses() {
        return courses;
    }

    public void setCourses(List<ProgramCoursesList> courses) {
        this.courses = courses;
    }

    @SerializedName("content_type")
    @Expose
    private String content_type;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("subtitle")
    @Expose
    private String subtitle;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("courses")
    @Expose
    private List<ProgramCoursesList> courses;

    public List<AuthoringOrganisations> getAuthoring_organizations() {
        return authoring_organizations;
    }

    public void setAuthoring_organizations(List<AuthoringOrganisations> authoring_organizations) {
        this.authoring_organizations = authoring_organizations;
    }

    @SerializedName("authoring_organizations")
    @Expose
    private List<AuthoringOrganisations> authoring_organizations;

    public boolean isProgramEnroll() {
        return programEnroll;
    }

    public void setProgramEnroll(boolean programEnroll) {
        this.programEnroll = programEnroll;
    }

    private boolean programEnroll;
}
