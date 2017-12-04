package org.edx.mobile.util;

import android.support.annotation.NonNull;

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
        return isSupportedVideoFormat(videoUrl, SUPPORTED_VIDEO_FORMATS);
    }

    /**
     * Check if format of video url exists in specified list of video formats.
     *
     * @param videoUrl         Video url whose format needs to be checked.
     * @param supportedFormats List of supported video formats.
     * @return <code>true</code> if video url format exist in supported formats list, <code>false</code> otherwise.
     */
    public static boolean isSupportedVideoFormat(@NonNull String videoUrl, @NonNull String... supportedFormats) {
        for (final String format : supportedFormats) {
            if (videoUrl.endsWith(format)) {
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
        final VideoInfo preferredVideoInfo = video.encodedVideos.getPreferredVideoInfo();
        return preferredVideoInfo != null &&
                !isSupportedVideoFormat(preferredVideoInfo.url, VIDEO_FORMAT_M3U8) &&
                !video.onlyOnWeb;
    }
}
