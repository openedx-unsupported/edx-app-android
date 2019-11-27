package org.edx.mobile.whatsnew;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.List;

public class WhatsNewModel implements Parcelable {
    @NonNull
    private String version;
    @NonNull
    private List<WhatsNewItemModel> messages;

    protected WhatsNewModel(Parcel in) {
        this.version = in.readString();
        this.messages = in.createTypedArrayList(WhatsNewItemModel.CREATOR);
    }

    @NonNull
    public String getVersion() {
        return version;
    }

    @NonNull
    public List<WhatsNewItemModel> getWhatsNewItems() {
        return messages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.version);
        dest.writeTypedList(this.messages);
    }

    public static final Creator<WhatsNewModel> CREATOR = new Creator<WhatsNewModel>() {
        @Override
        public WhatsNewModel createFromParcel(Parcel source) {
            return new WhatsNewModel(source);
        }

        @Override
        public WhatsNewModel[] newArray(int size) {
            return new WhatsNewModel[size];
        }
    };
}
