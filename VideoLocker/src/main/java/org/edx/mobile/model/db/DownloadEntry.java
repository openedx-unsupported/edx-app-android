package org.edx.mobile.model.db;

import android.content.Context;
import android.database.Cursor;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.api.EncodingsModel;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DbStructure;
import org.edx.mobile.module.prefs.PrefManager;

import android.text.TextUtils;

public class DownloadEntry implements SectionItemInterface, IVideoModel {

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
    public String lmsUrl;
    public TranscriptModel transcript;
    
    /**
     * Fields that are not part of database nor API
     */
    public boolean resumeFromLastLeftPosition = true;
    
    /**
     * Initializes all fields of this download entry from the values from given cursor.
     * @param c
     */
    public void initFrom(Context context, Cursor c) {
        dmId = c.getLong(c.getColumnIndex(DbStructure.Column.DM_ID));
        downloaded = DownloadedState.values()[c.getInt(c.getColumnIndex(DbStructure.Column.DOWNLOADED))];
        duration = c.getLong(c.getColumnIndex(DbStructure.Column.DURATION));
        filepath = c.getString(c.getColumnIndex(DbStructure.Column.FILEPATH));
        id = c.getInt(c.getColumnIndex(DbStructure.Column.ID));
        size = c.getLong(c.getColumnIndex(DbStructure.Column.SIZE));
        username = c.getString(c.getColumnIndex(DbStructure.Column.USERNAME));
        title = c.getString(c.getColumnIndex(DbStructure.Column.TITLE));
        url = c.getString(c.getColumnIndex(DbStructure.Column.URL));
        url_high_quality = c.getString(c.getColumnIndex(DbStructure.Column.URL_HIGH_QUALITY));
        url_low_quality = c.getString(c.getColumnIndex(DbStructure.Column.URL_LOW_QUALITY));
        url_youtube = c.getString(c.getColumnIndex(DbStructure.Column.URL_YOUTUBE));
        videoId = c.getString(c.getColumnIndex(DbStructure.Column.VIDEO_ID));
        watched = WatchedState.values()[c.getInt(c.getColumnIndex(DbStructure.Column.WATCHED))];
        eid = c.getString(c.getColumnIndex(DbStructure.Column.EID));
        chapter = c.getString(c.getColumnIndex(DbStructure.Column.CHAPTER));
        section = c.getString(c.getColumnIndex(DbStructure.Column.SECTION));
        downloadedOn = c.getLong(c.getColumnIndex(DbStructure.Column.DOWNLOADED_ON));
        lastPlayedOffset = c.getInt(c.getColumnIndex(DbStructure.Column.LAST_PLAYED_OFFSET));
        isCourseActive = c.getInt(c.getColumnIndex(DbStructure.Column.IS_COURSE_ACTIVE));
        try{
            lmsUrl = c.getString(c.getColumnIndex(DbStructure.Column.UNIT_URL));
            if(lmsUrl==null || lmsUrl.trim().length()==0){
                Api api = new Api(context);
                lmsUrl = api.getUnitUrlByVideoById(eid, videoId);
            }
        }catch(Exception e){
            new Logger(getClass().getName()).error(e);
        }
        
    } 

    /**
     * Returns duration in the format hh:mm:ss
     * @return
     */
    public String getDurationReadable() {
        if (duration == 0) {
            return "00:00";
        }
        
        // convert duration to seconds
        //long d = duration / 1000;
        long d = duration;
        int hours = (int) (d / 3600f); 
        d = d % 3600;
        int mins = (int) (d / 60f);
        int secs = (int) (d % 60); 
        if (hours <= 0) {
            return String.format("%02d:%02d", mins, secs);
        }
        return String.format("%02d:%02d:%02d", hours, mins, secs);
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
    public void setDownloadInfo(IVideoModel video) {
        dmId = video.getDmId();
        downloaded = DownloadedState.values()[video.getDownloadedStateOrdinal()];
        filepath = video.getFilePath();
        size = video.getSize();
        duration = video.getDuration();
    }
    
    public String getBestEncodingUrl(Context context){

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
