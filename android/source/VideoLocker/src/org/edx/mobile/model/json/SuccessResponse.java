package org.edx.mobile.model.json;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by marcashman on 2014-12-17.
 */
public class SuccessResponse implements Parcelable {
    private boolean success;

    public SuccessResponse(boolean success) {
        setSuccess(success);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isSuccess() ? (byte) 1 : (byte) 0);
    }

    private SuccessResponse(Parcel in) {
        setSuccess(in.readByte() != 0);
    }

    public static final Creator<SuccessResponse> CREATOR = new Creator<SuccessResponse>() {
        public SuccessResponse createFromParcel(Parcel source) {
            return new SuccessResponse(source);
        }

        public SuccessResponse[] newArray(int size) {
            return new SuccessResponse[size];
        }
    };
}
