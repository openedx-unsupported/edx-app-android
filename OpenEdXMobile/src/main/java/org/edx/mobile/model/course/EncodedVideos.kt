package org.edx.mobile.model.course

import android.webkit.URLUtil
import com.google.gson.annotations.SerializedName
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.VideoUtil
import java.io.Serializable

class EncodedVideos : Serializable {

    @JvmField
    @SerializedName("hls")
    var hls: VideoInfo? = null

    @SerializedName("fallback")
    var fallback: VideoInfo? = null

    @SerializedName("desktop_mp4")
    var desktopMp4: VideoInfo? = null

    @JvmField
    @SerializedName("mobile_high")
    var mobileHigh: VideoInfo? = null

    @JvmField
    @SerializedName("mobile_low")
    var mobileLow: VideoInfo? = null

    @JvmField
    @SerializedName("youtube")
    var youtube: VideoInfo? = null

    /**
     * Extract the Preferred [VideoInfo] for media playback and to store in database.
     *
     * @return Preferred [VideoInfo]
     */
    val preferredVideoInfo: VideoInfo?
        get() {
            if (isPreferredVideoInfo(hls)) {
                return hls
            }
            if (isPreferredVideoInfo(mobileLow)) {
                return mobileLow
            }
            if (isPreferredVideoInfo(mobileHigh)) {
                return mobileHigh
            }
            if (isPreferredVideoInfo(desktopMp4)) {
                return desktopMp4
            }
            return if (isPreferredVideoInfo(fallback)) {
                fallback
            } else null
        }

    /**
     * Extract the Preferred [VideoInfo] for media downloading.
     *
     * @param preferredVideoQuality [VideoQuality] selected by the user to download
     * @return Preferred [VideoInfo]
     */
    fun getPreferredVideoInfoForDownloading(preferredVideoQuality: VideoQuality): VideoInfo? {
        var preferredVideoInfo = when (preferredVideoQuality) {
            VideoQuality.OPTION_360P -> mobileLow
            VideoQuality.OPTION_540P -> mobileHigh
            VideoQuality.OPTION_720P -> desktopMp4
            else -> null
        }
        if (preferredVideoInfo == null) {
            preferredVideoInfo = getDefaultVideoInfoForDownloading()
        }
        return if (isPreferredVideoInfo(preferredVideoInfo)) {
            preferredVideoInfo
        } else {
            null
        }
    }

    private fun getDefaultVideoInfoForDownloading(): VideoInfo? {
        if (isPreferredVideoInfo(mobileLow)) {
            return mobileLow
        }
        if (isPreferredVideoInfo(mobileHigh)) {
            return mobileHigh
        }
        if (isPreferredVideoInfo(desktopMp4)) {
            return desktopMp4
        }
        fallback?.let {
            if (isPreferredVideoInfo(it) &&
                !VideoUtil.videoHasFormat(it.url, AppConstants.VIDEO_FORMAT_M3U8)
            ) {
                return fallback
            }
        }
        return null
    }

    private fun isPreferredVideoInfo(videoInfo: VideoInfo?): Boolean {
        return videoInfo != null &&
                URLUtil.isNetworkUrl(videoInfo.url) &&
                VideoUtil.isValidVideoUrl(videoInfo.url)
    }

    val youtubeVideoInfo: VideoInfo?
        get() = if (youtube != null && URLUtil.isNetworkUrl(youtube!!.url)) youtube else null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other == null || javaClass != other.javaClass) return false

        val that = other as EncodedVideos

        if (fallback != that.fallback) return false
        if (desktopMp4 != that.desktopMp4) return false
        if (mobileHigh != that.mobileHigh) return false

        return if (mobileLow != that.mobileLow) false else youtube == that.youtube
    }
}
