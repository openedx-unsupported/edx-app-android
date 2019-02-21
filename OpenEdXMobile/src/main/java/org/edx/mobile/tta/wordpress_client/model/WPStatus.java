package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Arjun Singh
 *         Created on 2015/12/03.
 */
public class WPStatus extends BaseModel {

    public static final int CLOSED = 0;
    public static final int OPEN = 1;

    private int status;

    public WPStatus() {
    }

    public WPStatus(Parcel in) {
        super(in);

        status = in.readInt();
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isClosed() {
        return status == CLOSED;
    }

    public boolean isOpen() {
        return status == OPEN;
    }

    public String getStatusString() {
        return isOpen() ? "open" : "closed";
    }

    public int getStatus() {
        return status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(status);
    }

    public static Parcelable.Creator<WPStatus> CREATOR = new Creator<WPStatus>() {
        @Override
        public WPStatus createFromParcel(Parcel source) {
            return new WPStatus(source);
        }

        @Override
        public WPStatus[] newArray(int size) {
            return new WPStatus[size];
        }
    };

    @Override
    public String toString() {
        return "WPStatus{" +
                "status=" + (status == CLOSED ? "closed" : "open") +
                '}';
    }
}
