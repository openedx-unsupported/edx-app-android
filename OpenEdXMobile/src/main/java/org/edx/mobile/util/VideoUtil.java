package org.edx.mobile.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.course.VideoData;
import org.edx.mobile.model.course.VideoInfo;

import static org.edx.mobile.util.AppConstants.VIDEO_FORMAT_M3U8;
import static org.edx.mobile.util.AppConstants.VIDEO_FORMAT_MP4;

public class VideoUtil {
    public static final String[] SUPPORTED_VIDEO_FORMATS = {
            VIDEO_FORMAT_MP4,
            VIDEO_FORMAT_M3U8
    };

    /**
     * Check the validity of video url.
     *
     * @param videoUrl Url which needs to be validated.
     * @return <code>true</code> if video url is valid, <code>false</code> otherwise.
     */
    public static boolean isValidVideoUrl(@NonNull String videoUrl) {
        return videoHasFormat(videoUrl, SUPPORTED_VIDEO_FORMATS);
    }

    /**
     * Check if format of video url exists in specified list of video formats.
     *
     * @param videoUrl         Video url whose format needs to be matched.
     * @param supportedFormats List of video formats.
     * @return <code>true</code> if video url format exist in specified formats list, <code>false</code> otherwise.
     */
    public static boolean videoHasFormat(@NonNull String videoUrl, @NonNull String... supportedFormats) {
        for (final String format : supportedFormats) {
            /*
             * Its better to find a video format extension in the whole url because there is a
             * possibility that extension exists somewhere in between of url for e.g.
             * https://player.vimeo.com/external/225003478.m3u8?s=6438b130458bd0eb38f7625ffa26623caee8ff7c
             */
            if (videoUrl.contains(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate if video is downloadable.
     *
     * @param video Data of the video whose download validity needs to be checked.
     * @return <code>true</code> if video is downloadable, <code>false</code> otherwise.
     */
    public static boolean isVideoDownloadable(@NonNull VideoData video) {
        return getPreferredVideoUrlForDownloading(video) != null;
    }

    /**
     * Gives the preferred video url for downloading from a specified {@link VideoData} object.
     *
     * @param video Data of the video whose preferred download url is needed.
     * @return download url of video, return null if unable to find any suitable url for downloading.
     */
    @Nullable
    public static String getPreferredVideoUrlForDownloading(@NonNull VideoData video) {
        final VideoInfo preferredVideoInfo = video.encodedVideos.getPreferredVideoInfoForDownloading();
        if (preferredVideoInfo == null || video.onlyOnWeb) {
            return null;
        }
        if (!videoHasFormat(preferredVideoInfo.url, VIDEO_FORMAT_M3U8)) {
            return preferredVideoInfo.url;
        }
        if (!new Config(MainApplication.instance()).isUsingVideoPipeline()) {
            /**
             * If preferred video url for downloading has HLS format and
             * {@link Config#USING_VIDEO_PIPELINE} feature flag is disabled, try to find some .mp4
             * format url from {@link VideoData#allSources} urls and consider it for download.
             */
            for (String url : video.allSources) {
                if (VideoUtil.videoHasFormat(url, AppConstants.VIDEO_FORMAT_MP4)) {
                    return url;
                }
            }
        }
        return null;
    }
}
