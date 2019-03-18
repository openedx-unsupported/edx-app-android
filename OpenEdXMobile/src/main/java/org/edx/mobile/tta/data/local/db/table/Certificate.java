package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;

import java.util.Date;

@Entity(tableName = "certificate", primaryKeys = {"course_id", "username"})
public class Certificate {

    private String username;

    private String status;

    private boolean regenerate;

    private Date created;

    private String grade;

    private String course_id;

    private String course_name;

    private String download_url;

    private Date modified;

    private String image;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
