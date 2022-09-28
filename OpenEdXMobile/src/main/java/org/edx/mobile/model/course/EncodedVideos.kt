package org.edx.mobile.model.course

import android.webkit.URLUtil
import com.google.gson.annotations.SerializedName
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.util.AppConstants
import org.edx.mobile.util.VideoUtil
import java.io.Serializable

data class EncodedVideos(
    @SerializedName("hls")
    val hls: VideoInfo?,

    @SerializedName("fallback")
    val fallback: VideoInfo?,

    @SerializedName("desktop_mp4")
    val desktopMp4: VideoInfo?,

    @SerializedName("mobile_high")
    val mobileHigh: VideoInfo?,

    @SerializedName("mobile_low")
    val mobileLow: VideoInfo?,

    @SerializedName("youtube")
    val youtube: VideoInfo?,
) : Serializable {

    val youtubeVideoInfo: VideoInfo?
        get() = if (youtube != null && URLUtil.isNetworkUrl(youtube.url)) youtube else null

    /**
     * Extract the Preferred Native [VideoInfo] based on their stream priority for media playback
     * to store in database model.
     *
     * @return Preferred Native [VideoInfo] having the minimum value of Stream priority or null
     */
    val preferredNativeVideoInfo: VideoInfo?
        get() = getPreferredVideoInfo(nativeEncodedVideos)

    /**
     * Extract the Preferred [VideoInfo] based on their stream priority for media playback
     * including youtube
     *
     * @return Preferred [VideoInfo] having the minimum stream priority value
     */
    val preferredVideoInfo: VideoInfo?
        get() {
            nativeEncodedVideos.toMutableList().apply {
                youtubeVideoInfo?.let { this.add(it) }
                return getPreferredVideoInfo(this)
            }
        }

    /**
     * Populate and return a list of all the available Native Encoded Videos
     */
    private val nativeEncodedVideos: List<VideoInfo>
        get() = listOf(hls, mobileLow, mobileHigh, desktopMp4, fallback)
            .filter { isNativeVideoInfo(it) }
            .filterNotNull()

    /**
     * Method to check if the [VideoInfo] is a valid Native VideoInfo or not
     */
    private fun isNativeVideoInfo(videoInfo: VideoInfo?): Boolean {
        return videoInfo != null &&
                URLUtil.isNetworkUrl(videoInfo.url) &&
                VideoUtil.isValidVideoUrl(videoInfo.url)
    }

    /**
     * Method to filter out the Encoded Video with lowest stream priority
     *
     * @param encodedVideos list of [EncodedVideos]
     * @return [VideoInfo] having the minimum value Stream priority or null
     */
    private fun getPreferredVideoInfo(encodedVideos: List<VideoInfo>): VideoInfo? {
        return encodedVideos
            .filter { it.streamPriority > VideoInfo.DEFAULT_STREAM_PRIORITY }
            .minByOrNull { it.streamPriority }
            ?: nativeEncodedVideos.firstOrNull()
    }

    /**
     * Extract the Preferred [VideoInfo] for media downloading.
     *
     * @param preferredVideoQuality [VideoQuality] selected by the user to download
     * @return Preferred [VideoInfo]
     */
    fun getPreferredVideoInfoForDownloading(preferredVideoQuality: VideoQuality): VideoInfo? {
        val preferredVideoInfo = when (preferredVideoQuality) {
            VideoQuality.OPTION_360P -> mobileLow
            VideoQuality.OPTION_540P -> mobileHigh
            VideoQuality.OPTION_720P -> desktopMp4
            else -> null
        }
        return if (isNativeVideoInfo(preferredVideoInfo)) preferredVideoInfo
        else getDefaultVideoInfoForDownloading()
    }

    private fun getDefaultVideoInfoForDownloading(): VideoInfo? {
        if (isNativeVideoInfo(mobileLow)) {
            return mobileLow
        }
        if (isNativeVideoInfo(mobileHigh)) {
            return mobileHigh
        }
        if (isNativeVideoInfo(desktopMp4)) {
            return desktopMp4
        }
        if (fallback != null
            && URLUtil.isNetworkUrl(fallback.url)
            && VideoUtil.videoHasFormat(fallback.url, AppConstants.VIDEO_FORMAT_MP4)
        ) {
            return fallback
        }
        return null
    }
}
