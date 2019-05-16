package org.edx.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "unit_status",
        primaryKeys = {"unit_id", "course_id", "username"})
public class UnitStatus {

    private String status;

    @NonNull
    private String unit_id;

    @NonNull
    private String course_id;

    @NonNull
    private String username;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @NonNull
    public String getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(@NonNull String unit_id) {
        this.unit_id = unit_id;
    }

    @NonNull
    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(@NonNull String course_id) {
        this.course_id = course_id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }
}
