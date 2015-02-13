package org.edx.mobile.module.db;

import android.database.Cursor;

import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;

/**
 * Model Factory class for the database models.
 * @author rohan
 *
 */
public class DatabaseModelFactory {

    /**
     * Returns new instance of {@link org.edx.mobile.model.IVideoModel} initialized with given cursor.
     * @param c
     * @return
     */
    public static IVideoModel getModel(Cursor c) {
        DownloadEntry de = new DownloadEntry();
        
        de.dmId = c.getLong(c.getColumnIndex(DbStructure.Column.DM_ID));
        de.downloaded = DownloadEntry.DownloadedState.values()[c.getInt(c.getColumnIndex(DbStructure.Column.DOWNLOADED))];
        de.duration = c.getLong(c.getColumnIndex(DbStructure.Column.DURATION));
        de.filepath = c.getString(c.getColumnIndex(DbStructure.Column.FILEPATH));
        de.id = c.getInt(c.getColumnIndex(DbStructure.Column.ID));
        de.size = c.getLong(c.getColumnIndex(DbStructure.Column.SIZE));
        de.username = c.getString(c.getColumnIndex(DbStructure.Column.USERNAME));
        de.title = c.getString(c.getColumnIndex(DbStructure.Column.TITLE));
        de.url = c.getString(c.getColumnIndex(DbStructure.Column.URL));
        de.url_high_quality = c.getString(c.getColumnIndex(DbStructure.Column.URL_HIGH_QUALITY));
        de.url_low_quality = c.getString(c.getColumnIndex(DbStructure.Column.URL_LOW_QUALITY));
        de.url_youtube = c.getString(c.getColumnIndex(DbStructure.Column.URL_YOUTUBE));
        de.videoId = c.getString(c.getColumnIndex(DbStructure.Column.VIDEO_ID));
        de.watched = DownloadEntry.WatchedState.values()[c.getInt(c.getColumnIndex(DbStructure.Column.WATCHED))];
        de.eid = c.getString(c.getColumnIndex(DbStructure.Column.EID));
        de.chapter = c.getString(c.getColumnIndex(DbStructure.Column.CHAPTER));
        de.section = c.getString(c.getColumnIndex(DbStructure.Column.SECTION));
        de.downloadedOn = c.getLong(c.getColumnIndex(DbStructure.Column.DOWNLOADED_ON));
        de.lastPlayedOffset = c.getInt(c.getColumnIndex(DbStructure.Column.LAST_PLAYED_OFFSET));
        de.isCourseActive = c.getInt(c.getColumnIndex(DbStructure.Column.IS_COURSE_ACTIVE));
        de.lmsUrl = c.getString(c.getColumnIndex(DbStructure.Column.UNIT_URL));
        
        return de;
    }

    /**
     * Returns an object of IVideoModel which has all the fields copied from given VideoResponseModel.
     * @param vrm
     * @return
     */
    public static IVideoModel getModel(VideoResponseModel vrm) {
        DownloadEntry e = new DownloadEntry();
        e.chapter = vrm.getChapterName();
        e.section = vrm.getSequentialName();
        e.eid = vrm.getCourseId();
        e.duration = vrm.getSummary().getDuration();
        e.size = vrm.getSummary().getSize();
        e.title = vrm.getSummary().getName();
        e.url = vrm.getSummary().getVideo_url();
        e.url_high_quality = vrm.getSummary().getHighEncoding();
        e.url_low_quality = vrm.getSummary().getLowEncoding();
        e.url_youtube = vrm.getSummary().getYoutubeLink();
        e.videoId = vrm.getSummary().getId();
        e.transcript = vrm.getSummary().getTranscripts();
        e.lmsUrl = vrm.unit_url;

        e.videoId = vrm.getSummary().getId();
        e.transcript = vrm.getSummary().getTranscripts();
        e.lmsUrl = vrm.unit_url;
        return e;
    }
}
