package org.edx.mobile.model.course;

import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.URLUtil;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EncodedVideos implements Serializable {
    @SerializedName("mobile_high")
    public VideoInfo mobileHigh;

    @SerializedName("mobile_low")
    public VideoInfo mobileLow;

    @SerializedName("youtube")
    public VideoInfo youtube;

    @SerializedName("hls")
    public VideoInfo hls;

    @SerializedName("fallback")
    public VideoInfo fallback;

    @Nullable
    public VideoInfo getPreferredVideoInfo() {
        if (mobileLow != null && URLUtil.isNetworkUrl(mobileLow.url))
            return mobileLow;
        if (mobileHigh != null && URLUtil.isNetworkUrl(mobileHigh.url))
            return mobileHigh;
        if (hls != null && URLUtil.isNetworkUrl(hls.url))
            return hls;
        if (fallback != null && URLUtil.isNetworkUrl(fallback.url))
            return fallback;
        return null;
    }

    @Nullable
    public VideoInfo getDownloadableVideoInfo() {
        if (mobileLow != null && URLUtil.isNetworkUrl(mobileLow.url))
            return mobileLow;
        if (mobileHigh != null && URLUtil.isNetworkUrl(mobileHigh.url))
            return mobileHigh;
        if (fallback != null && URLUtil.isNetworkUrl(fallback.url))
            return fallback;
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
        if (hls != null ? !hls.equals(that.hls) : that.hls != null)
            return false;
        if (fallback != null ? !fallback.equals(that.fallback) : that.fallback != null)
            return false;
        return youtube != null ? youtube.equals(that.youtube) : that.youtube == null;

    }
}
