package org.edx.mobile.model.db;

import android.content.Context;
import android.text.TextUtils;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.api.EncodingsModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.JavaUtil;

public class DownloadEntry implements SectionItemInterface, VideoModel {

    public static enum WatchedState { UNWATCHED, PARTIALLY_WATCHED, WATCHED}
    public static enum DownloadedState { DOWNLOADING, DOWNLOADED, ONLINE }

    public int id;
    public String username;
    public String title;
    public String filepath;
    public long size;
    public long duration;
    // default unwatched
    public WatchedState watched = WatchedState.UNWATCHED;
    // default not_downloaded
    public DownloadedState downloaded = DownloadedState.ONLINE;
    public String videoId;
    public String url;
    public String url_hls;
    public String url_high_quality;
    public String url_low_quality;
    public String url_youtube;
    public long dmId = -1;
    // enrollment id
    public String eid;
    public String chapter;
    public String section;
    public long downloadedOn;
    public int lastPlayedOffset;
    public int isCourseActive = 1; // default is TRUE
    public boolean isVideoForWebOnly; //default is FALSE
    public String lmsUrl;
    public TranscriptModel transcript;

    @Inject
    IEdxEnvironment environment;

    /**
     * Returns duration in the readable format i.e. hh:mm:ss. Returns null if duration is zero or
     * negative.
     *
     * @return Formatted duration.
     */
    public String getDurationReadable() {
        return JavaUtil.getDurationString(duration);
    }

    @Override
    public boolean isChapter() {
        // video model is never a chapter
        return false;
    }

    @Override
    public boolean isSection() {
        // video model is never a section
        return false;
    }
    
    @Override
    public boolean isCourse() {
        return false;
    }

    @Override
    public boolean isVideo() {
        return false;
    }

    @Override
    public boolean isDownload() {
        return true;
    }
    
    /**
     * Returns true if this video is downloaded successfully, false otherwise.
     * @return
     */
    public boolean isDownloaded() {
        return (downloaded == DownloadedState.DOWNLOADED);
    }
    
    @Override
    public String toString() {
        return String.format("dmid=%d, title=%s, path=%s, url=%s, size=%d, duration=%d", dmId, title, filepath, url, size, duration);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getTitle() {
        if (title == null || title.trim().length() == 0) {
            return "(Untitled)";
        }
        return title;
    }

    @Override
    public String getVideoId() {
        return videoId;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getFilePath() {
        return filepath;
    }

    @Override
    public String getVideoUrl() {
        return url;
    }

    @Override
    public String getHLSVideoUrl() {
        return url_hls;
    }

    @Override
    public String getHighQualityVideoUrl() {
        return url_high_quality;
    }

    @Override
    public String getLowQualityVideoUrl() {
        return url_low_quality;
    }

    @Override
    public String getYoutubeVideoUrl() {
        return url_youtube;
    }

    @Override
    public int getWatchedStateOrdinal() {
        return watched.ordinal();
    }

    @Override
    public int getDownloadedStateOrdinal() {
        return downloaded.ordinal();
    }

    @Override
    public long getDmId() {
        return dmId;
    }

    @Override
    public String getEnrollmentId() {
        return eid;
    }

    @Override
    public String getChapterName() {
        return chapter;
    }

    @Override
    public String getSectionName() {
        return section;
    }

    @Override
    public int getLastPlayedOffset() {
        return lastPlayedOffset;
    }

    @Override
    public String getLmsUrl() {
        return lmsUrl;
    }

    @Override
    public boolean isCourseActive() {
        return (isCourseActive == 1);
    }

    @Override
    public boolean  isVideoForWebOnly() { return isVideoForWebOnly; }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public long getDownloadedOn() {
        return downloadedOn;
    }

    @Override
    public TranscriptModel getTranscripts() {
        return transcript;
    }

    @Override
    public void setDownloadInfo(NativeDownloadModel download) {
        dmId = download.dmid;
        downloaded = DownloadedState.DOWNLOADING;
        filepath = download.filepath;
        size = download.size;
        // duration can't be updated here
    }
    
    @Override
    public void setDownloadingInfo(NativeDownloadModel download) {
        dmId = download.dmid;
        downloaded = DownloadedState.DOWNLOADING;
        // duration can't be updated here
    }

    @Override
    public void setDownloadInfo(VideoModel video) {
        dmId = video.getDmId();
        downloaded = DownloadedState.values()[video.getDownloadedStateOrdinal()];
        filepath = video.getFilePath();
        size = video.getSize();
        duration = video.getDuration();
    }

    public String getBestEncodingUrl(Context context) {
        if (!TextUtils.isEmpty(url_hls)) {
            return url_hls;
        }

        PrefManager prefs = new PrefManager(context, PrefManager.Pref.WIFI);
        float kbs = prefs.getFloat(PrefManager.Key.SPEED_TEST_KBPS, 0.0f);
        float thresholdKps = (float)context.getResources().getInteger(R.integer.threshold_kbps_for_video);

        EncodingsModel.EncodingLevel level = kbs > thresholdKps ?
                EncodingsModel.EncodingLevel.HIGH : EncodingsModel.EncodingLevel.LOW;

        switch (level) {
            case HIGH:
                if (!TextUtils.isEmpty(url_high_quality)){
                    return url_high_quality;
                }
                break;
            case LOW:
                if (!TextUtils.isEmpty(url_low_quality)){
                    return url_low_quality;
                }
                break;
        }

        return getVideoUrl();
    }
}
