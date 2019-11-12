package org.humana.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "unit")
public class Unit {

    @NonNull
    @PrimaryKey
    private String id;

    private String title;

    private String code;

    private String status;

    private String programId;

    private String sectionId;

    public int getUnitHour() {
        return unitHour;
    }

    public void setUnitHour(int unitHour) {
        this.unitHour = unitHour;
    }

    private int unitHour;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public long getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(long statusDate) {
        this.statusDate = statusDate;
    }

    private long statusDate;

    @SerializedName("period_id")
    private long periodId;

    @SerializedName("period_name")
    private String periodName;

    private String unit_id;

    @SerializedName("my_date")
    private long myDate;

    @SerializedName("staff_date")
    private long staffDate;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private String desc;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public long getPeriodId() {
        return periodId;
    }

    public void setPeriodId(long periodId) {
        this.periodId = periodId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(String unit_id) {
        this.unit_id = unit_id;
    }

    public long getMyDate() {
        return myDate;
    }

    public void setMyDate(long myDate) {
        this.myDate = myDate;
    }

    public long getStaffDate() {
        return staffDate;
    }

    public void setStaffDate(long staffDate) {
        this.staffDate = staffDate;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Unit && (((Unit) obj).id.equals(id));
    }
}
