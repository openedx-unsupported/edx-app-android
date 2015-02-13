package org.edx.mobile.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by marcashman on 2014-12-02.
 */
public class ProgressReport implements Parcelable {
    /**
     * downloaded in kBps
     */
    private double downloaded;

    /**
     * time in nanoseconds
     */
    private long time;

    public ProgressReport(double downloaded, long time) {
        this.downloaded = downloaded;
        this.time = time;
    }

    public double getDownloaded() {
        return downloaded;
    }

    public long getTime() {
        return time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.downloaded);
        dest.writeLong(this.time);
    }

    ProgressReport(Parcel in) {
        this.downloaded = in.readDouble();
        this.time = in.readLong();
    }

    public static final Creator<ProgressReport> CREATOR = new Creator<ProgressReport>() {
        public ProgressReport createFromParcel(Parcel source) {
            return new ProgressReport(source);
        }

        public ProgressReport[] newArray(int size) {
            return new ProgressReport[size];
        }
    };
}

