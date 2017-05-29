package org.edx.mobile.whatsnew;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class WhatsNewModel implements Parcelable {
    @NonNull
    private String title;
    @NonNull
    private String message;
    @NonNull
    private String image;

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public void setMessage(@NonNull String message) {
        this.message = message;
    }

    @NonNull
    public String getImage() {
        return image;
    }

    public void setImage(@NonNull String image) {
        this.image = image;
    }

    protected WhatsNewModel(Parcel in) {
        title = in.readString();
        message = in.readString();
        image = in.readString();
    }

    public static final Creator<WhatsNewModel> CREATOR = new Creator<WhatsNewModel>() {
        @Override
        public WhatsNewModel createFromParcel(Parcel in) {
            return new WhatsNewModel(in);
        }

        @Override
        public WhatsNewModel[] newArray(int size) {
            return new WhatsNewModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(message);
        parcel.writeString(image);
    }
}
