package org.edx.mobile.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.course.VideoData;
import org.edx.mobile.model.course.VideoInfo;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;

import java.io.File;

import static org.edx.mobile.util.AppConstants.VIDEO_FORMAT_M3U8;
import static org.edx.mobile.util.AppConstants.VIDEO_FORMAT_MP4;
import static org.edx.mobile.util.AppConstants.YOUTUBE_PACKAGE_NAME;

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
        String preferredVideoUrl = null;
        final VideoInfo preferredVideoInfo = video.encodedVideos.getPreferredVideoInfoForDownloading();

        if (preferredVideoInfo != null &&
                !TextUtils.isEmpty(preferredVideoInfo.url) &&
                !video.onlyOnWeb &&
                !videoHasFormat(preferredVideoInfo.url, VIDEO_FORMAT_M3U8)) {
            return preferredVideoInfo.url;
        }

        if (!new Config(MainApplication.instance()).isUsingVideoPipeline()) {
            /*
              If {@link Config#USING_VIDEO_PIPELINE} feature flag is disabled, try to find some .mp4
              format url from {@link VideoData#allSources} urls and consider it for download.
             */
            for (String url : video.allSources) {
                if (VideoUtil.videoHasFormat(url, AppConstants.VIDEO_FORMAT_MP4)) {
                    preferredVideoUrl = url;
                }
            }
        }
        return preferredVideoUrl;
    }

    /**
     * Utility method to update the downloaded video file info and status in database.
     *
     * @param db
     * @param videoModel    Video info need to update in database.
     * @param downloadState New file status(DOWNLOADED / ONLINE).
     */
    public static void updateVideoDownloadState(IDatabase db, VideoModel videoModel,
                                                int downloadState) {
        final NativeDownloadModel dm = new NativeDownloadModel();
        dm.dmid = videoModel.getDmId();
        dm.filepath = videoModel.getFilePath();
        dm.size = videoModel.getSize();
        dm.downloaded = downloadState;
        videoModel.setDownloadInfo(dm);
        db.updateDownloadingVideoInfoByVideoId(videoModel, null);
    }

    /**
     * @param context Current Activity context
     * @param video   {@link DownloadEntry} object having different video encodings
     * @return Best encoding video url that can be locally downloaded path or online url
     */
    public static String getVideoPath(Context context, DownloadEntry video) {
        String filepath = null;
        if (video.filepath != null && video.filepath.length() > 0) {
            if (video.isDownloaded()) {
                final File f = new File(video.filepath);
                if (f.exists()) {
                    // play from local
                    filepath = video.filepath;
                }
            }
        } else {
            final DownloadEntry de = (DownloadEntry) DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE)
                    .getIVideoModelByVideoUrl(
                            video.url, null);
            if (de != null) {
                if (de.filepath != null) {
                    final File f = new File(de.filepath);
                    if (f.exists()) {
                        // play from local
                        filepath = de.filepath;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(filepath)) {
            // not available on local, so play online
            filepath = video.getBestEncodingUrl(context);
        }
        return filepath;
    }

    /**
     * Utility method to check that the supported YouTube app is installed or not, because YouTube
     * in-app player SDK works only if supported version of YouTube app is installed.
     *
     * @param context The activity context
     * @return true if the requirements are satisfied otherwise false
     */
    public static boolean isYoutubeAPISupported(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(YOUTUBE_PACKAGE_NAME, 0);
            /*
             * Youtube documentation says "Users need to run version 4.2.16 of the mobile YouTube
             * app (or higher) to use the API."
             * But the sdk is not working even for youtube app version 10.18.55,
             * With testing on different versions it has been figured out that the version of
             * youtube app should be greater than or equal to 12.0.0
             *
             * Available Refs:
             * - https://developers.google.com/youtube/android/player/
             * - https://stackoverflow.com/a/37553255
             *
             */
            final float targetVersion = 12;
            final float currentVersion = Float.parseFloat(packageInfo.versionName.split("\\.")[0]);
            return Float.compare(currentVersion, targetVersion) >= 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
