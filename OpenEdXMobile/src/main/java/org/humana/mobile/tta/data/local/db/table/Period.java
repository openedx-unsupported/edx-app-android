package org.humana.mobile.tta.data.local.db.table;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "period")
public class Period implements Parcelable {

    @PrimaryKey
    private long id;

    private String title;

    private String code;

    private long weeks;

    private String language;

    @SerializedName("completed_count")
    private long completedCount;

    @SerializedName("total_count")
    private long totalCount;

    private String username;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @SerializedName("desc")
    private String desc;

    public String getAbout_url() {
        return about_url;
    }

    public void setAbout_url(String about_url) {
        this.about_url = about_url;
    }

    @SerializedName("about_url")
    private String about_url;

    @SerializedName("startDate")
    private long startDate;

    @SerializedName("endDate")
    private long endDate;

    @SerializedName("total_points")
    private int total_points;

    public int getTotal_points() {
        return total_points;
    }

    public void setTotal_points(int total_points) {
        this.total_points = total_points;
    }

    public int getCompleted_points() {
        return completed_points;
    }

    public void setCompleted_points(int completed_points) {
        this.completed_points = completed_points;
    }

    @SerializedName("completed_points")
    private int completed_points;



    public String getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    private String downloadStatus;

    public Period() {
    }

    protected Period(Parcel in) {
        id = in.readLong();
        title = in.readString();
        code = in.readString();
        weeks = in.readLong();
        completedCount = in.readLong();
        totalCount = in.readLong();
        username = in.readString();
        language = in.readString();
        desc = in.readString();
        about_url = in.readString();
        startDate = in.readLong();
        endDate = in.readLong();
        total_points = in.readInt();
        completed_points = in.readInt();
    }

    public static final Creator<Period> CREATOR = new Creator<Period>() {
        @Override
        public Period createFromParcel(Parcel in) {
            return new Period(in);
        }

        @Override
        public Period[] newArray(int size) {
            return new Period[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public long getWeeks() {
        return weeks;
    }

    public void setWeeks(long weeks) {
        this.weeks = weeks;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(code);
        dest.writeLong(weeks);
        dest.writeLong(completedCount);
        dest.writeLong(totalCount);
        dest.writeString(username);
        dest.writeString(language);
        dest.writeString(desc);
        dest.writeString(about_url);
        dest.writeLong(startDate);
        dest.writeLong(endDate);
        dest.writeInt(total_points);
        dest.writeInt(completed_points);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof Period) && (((Period) obj).id == id);
    }


}
