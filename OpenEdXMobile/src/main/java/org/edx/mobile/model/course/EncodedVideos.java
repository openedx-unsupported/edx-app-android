package org.edx.mobile.model.course;

import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.VideoUtil;

import java.io.Serializable;

import static org.edx.mobile.util.AppConstants.VIDEO_FORMAT_M3U8;

public class EncodedVideos implements Serializable {

    @SerializedName("hls")
    public VideoInfo hls;

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
        if (isPreferredVideoInfo(hls)) {
            return hls;
        }
        if (isPreferredVideoInfo(mobileLow)) {
            return mobileLow;
        }
        if (isPreferredVideoInfo(mobileHigh)) {
            return mobileHigh;
        }
        if (new Config(MainApplication.instance()).isUsingVideoPipeline()) {
            if (fallback != null && URLUtil.isNetworkUrl(fallback.url) &&
                    VideoUtil.videoHasFormat(fallback.url, AppConstants.VIDEO_FORMAT_M3U8)) {
                return fallback;
            }
        } else {
            if (isPreferredVideoInfo(fallback)) {
                return fallback;
            }
        }
        return null;
    }

    @Nullable
    public VideoInfo getPreferredVideoInfoForDownloading() {
        if (isPreferredVideoInfo(mobileLow)) {
            return mobileLow;
        }
        if (isPreferredVideoInfo(mobileHigh)) {
            return mobileHigh;
        }
        if (!new Config(MainApplication.instance()).isUsingVideoPipeline() &&
                isPreferredVideoInfo(fallback) &&
                !VideoUtil.videoHasFormat(fallback.url, VIDEO_FORMAT_M3U8)) {
            return fallback;
        }
        return null;
    }

    private boolean isPreferredVideoInfo(@Nullable VideoInfo videoInfo) {
        return videoInfo != null &&
                URLUtil.isNetworkUrl(videoInfo.url) &&
                VideoUtil.isValidVideoUrl(videoInfo.url);
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

        if (fallback != null ? !fallback.equals(that.fallback) : that.fallback != null)
            return false;
        if (mobileHigh != null ? !mobileHigh.equals(that.mobileHigh) : that.mobileHigh != null)
            return false;
        if (mobileLow != null ? !mobileLow.equals(that.mobileLow) : that.mobileLow != null)
            return false;
        return youtube != null ? youtube.equals(that.youtube) : that.youtube == null;

    }
}
