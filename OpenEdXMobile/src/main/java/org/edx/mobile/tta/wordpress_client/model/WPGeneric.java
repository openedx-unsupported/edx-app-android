package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Generic class for holding "rendered" fields returned by the JSON API
 *
 * @author Arjun Singh
 *         Created on 2015/12/03.
 */
public class WPGeneric extends BaseModel {

    @SerializedName("rendered")
    private String rendered;

    public String getRendered() {
        if(TextUtils.isEmpty(rendered))
            rendered="";
        return rendered;
    }

    public void setRendered(String rendered) {
        this.rendered = rendered;
    }

    public WPGeneric withRendered(String rendered) {
        this.rendered = rendered;
        return this;
    }

    @SerializedName("raw")
    private String raw;

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public WPGeneric withRaw(String raw) {
        setRaw(raw);
        return this;
    }

    public WPGeneric() {
    }

    public WPGeneric(Parcel in) {
        super(in);

        rendered = in.readString();
        raw = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(rendered);
        dest.writeString(raw);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<WPGeneric> CREATOR = new Creator<WPGeneric>() {
        @Override
        public WPGeneric createFromParcel(Parcel source) {
            return new WPGeneric(source);
        }

        @Override
        public WPGeneric[] newArray(int size) {
            return new WPGeneric[size];
        }
    };

    @Override
    public String toString() {
        return "WPGeneric{" +
                "rendered='" + rendered + '\'' +
                ", raw='" + raw + '\'' +
                '}';
    }
}
