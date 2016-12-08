package org.edx.mobile.model.course;

import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;

import org.edx.mobile.core.EdxEnvironment;

import java.io.Serializable;

public class EncodedVideos implements Serializable {
    @SerializedName("fallback")
    public VideoInfo fallback;

    @SerializedName("mobile_high")
    public VideoInfo mobileHigh;

    @SerializedName("mobile_low")
    public VideoInfo mobileLow;

    @SerializedName("youtube")
    public VideoInfo youtube;

    @Nullable
    public VideoInfo getMobileVideoInfo() {
        if (mobileLow != null && URLUtil.isNetworkUrl(mobileLow.url))
            return mobileLow;
        if (mobileHigh != null && URLUtil.isNetworkUrl(mobileHigh.url))
            return mobileHigh;
        return null;
    }

    @Nullable
    public VideoInfo getYoutubeVideoInfo() {
        if (youtube != null && URLUtil.isNetworkUrl(youtube.url))
            return youtube;
        return null;
    }

    @Nullable
    public VideoInfo getFallbackVideoInfo() {
        if (fallback != null && URLUtil.isNetworkUrl(fallback.url))
            return fallback;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodedVideos that = (EncodedVideos) o;

        if (fallback != null ? !fallback.equals(that.fallback) : that.fallback != null)
            return false;
        if (mobileHigh != null ? !mobileHigh.equals(that.mobileHigh) : that.mobileHigh != null)
            return false;
        if (mobileLow != null ? !mobileLow.equals(that.mobileLow) : that.mobileLow != null)
            return false;
        return youtube != null ? youtube.equals(that.youtube) : that.youtube == null;

    }
}
