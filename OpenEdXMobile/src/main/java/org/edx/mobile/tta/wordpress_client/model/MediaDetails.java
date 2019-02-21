package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Arjun Singh
 *         Created on 2016/01/07.
 */
public class MediaDetails extends BaseModel {

    private int width;

    private int height;

    private String file;

    // TODO add 'sizes' and 'image_meta' data

    public MediaDetails() {
    }

    public MediaDetails(Parcel in) {
        super(in);
        width = in.readInt();
        height = in.readInt();
        file = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(file);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MediaDetails> CREATOR = new Creator<MediaDetails>() {
        @Override
        public MediaDetails createFromParcel(Parcel source) {
            return new MediaDetails(source);
        }

        @Override
        public MediaDetails[] newArray(int size) {
            return new MediaDetails[size];
        }
    };

}
