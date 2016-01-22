package org.edx.mobile.model.course;

import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.google.gson.annotations.SerializedName;

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
    public VideoInfo getPreferredVideoInfo() {
        if (mobileLow != null && URLUtil.isNetworkUrl(mobileLow.url))
            return mobileLow;
        if (mobileHigh != null && URLUtil.isNetworkUrl(mobileHigh.url))
            return mobileHigh;
        if (fallback != null && URLUtil.isNetworkUrl(fallback.url))
            return fallback;
        return null;
    }
}
