package org.edx.mobile.model.api;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AnnouncementsModel implements Serializable, Parcelable {

    public String date;
    public String content;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public AnnouncementsModel(Parcel in){
        date = in.readString();
        content = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(content);
    }

    public static final Creator<AnnouncementsModel> CREATOR
            = new Creator<AnnouncementsModel>() {
        public AnnouncementsModel createFromParcel(Parcel in) {
            return new AnnouncementsModel(in);
        }

        public AnnouncementsModel[] newArray(int size) {
            return new AnnouncementsModel[size];
        }
    };
}
