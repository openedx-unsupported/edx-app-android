package org.edx.mobile.model.course;

import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EncodedVideos implements Serializable {
    @SerializedName("mobile_high")
    public VideoInfo mobileHigh;

    @SerializedName("mobile_low")
    public VideoInfo mobileLow;

    @SerializedName("fallback")
    public VideoInfo fallBack;

    @SerializedName("youtube")
    public VideoInfo youtube;

    @Nullable
    public VideoInfo getPreferredVideoInfo() {
        if (mobileLow != null && URLUtil.isNetworkUrl(mobileLow.url))
            return mobileLow;
        if (mobileHigh != null && URLUtil.isNetworkUrl(mobileHigh.url))
            return mobileHigh;
        if (fallBack != null && URLUtil.isNetworkUrl(fallBack.url))
            return fallBack;
        return null;
    }

    @Nullable
    public VideoInfo getYoutubeVideoInfo() {
        if (youtube != null && URLUtil.isNetworkUrl(youtube.url))
            return youtube;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodedVideos that = (EncodedVideos) o;

        if (mobileHigh != null ? !mobileHigh.equals(that.mobileHigh) : that.mobileHigh != null)
            return false;
        if (mobileLow != null ? !mobileLow.equals(that.mobileLow) : that.mobileLow != null)
            return false;
        if (fallBack != null ? !fallBack.equals(that.fallBack) : that.fallBack != null)
            return false;
        return youtube != null ? youtube.equals(that.youtube) : that.youtube == null;

    }
}
