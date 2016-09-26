package org.edx.mobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by marcashman on 2014-11-21.
 */
public class DownloadDescriptor implements Parcelable {
    private String url;
    private boolean forceDownload = false;

    public DownloadDescriptor(String url, boolean forceDownload) {
        this.url = url;
        this.forceDownload = forceDownload;
    }

    public String getUrl() {
        return url;
    }

    public boolean shouldForceDownload() {
        return forceDownload;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeBooleanArray(new boolean[]{forceDownload});
    }

    private DownloadDescriptor(Parcel in) {
        this.url = in.readString();
        boolean[] forceDownload = new boolean[1];
        in.readBooleanArray(forceDownload);
        this.forceDownload = forceDownload != null && forceDownload.length > 0 && forceDownload[0];
    }

    public static final Creator<DownloadDescriptor> CREATOR = new Creator<DownloadDescriptor>() {
        public DownloadDescriptor createFromParcel(Parcel source) {
            return new DownloadDescriptor(source);
        }

        public DownloadDescriptor[] newArray(int size) {
            return new DownloadDescriptor[size];
        }
    };
}
