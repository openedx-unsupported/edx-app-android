package org.edx.mobile.social;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Class is implementing Serializable in order to be included inside of CourseEntry. Ideally CourseEntry would implement Parcelable instead.

public class SocialMember implements Parcelable, Serializable {

    protected long id;
    @SerializedName("name")
    protected String fullName;
    protected String pictureUrl;

    public SocialMember(long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public SocialMember(long id, String firstName, String lastName) {
        this(id, firstName + " " + lastName);
    }

    public SocialMember(long id, String firstName, String lastName, String pictureUrl) {
        this(id, firstName, lastName);
        this.pictureUrl = pictureUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.fullName);
        dest.writeString(this.pictureUrl);
    }

    public SocialMember() {
    }

    private SocialMember(Parcel in) {
        this.id = in.readLong();
        this.fullName = in.readString();
        this.pictureUrl = in.readString();
    }

    public static final Creator<SocialMember> CREATOR = new Creator<SocialMember>() {
        public SocialMember createFromParcel(Parcel source) {
            return new SocialMember(source);
        }

        public SocialMember[] newArray(int size) {
            return new SocialMember[size];
        }
    };
}
