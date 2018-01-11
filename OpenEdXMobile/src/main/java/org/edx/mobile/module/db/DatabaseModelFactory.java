package org.edx.mobile.module.db;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import org.edx.mobile.model.AudioModel;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.AudioBlockModel;
import org.edx.mobile.model.course.AudioData;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.course.VideoData;
import org.edx.mobile.model.course.VideoInfo;
import org.edx.mobile.model.db.DownloadEntry;

/**
 * Model Factory class for the database models.
 *
 * @author rohan
 */
public class DatabaseModelFactory {

    /**
     * Returns new instance of {@link org.edx.mobile.model.VideoModel} initialized with given cursor.
     *
     * @param c
     * @return
     */
    public static DownloadEntry getModel(Cursor c) {
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
        de.url_mp3 = c.getString(c.getColumnIndex(DbStructure.Column.URL_MP3));
        de.url_ogg = c.getString(c.getColumnIndex(DbStructure.Column.URL_OGG));
        de.blockId = c.getString(c.getColumnIndex(DbStructure.Column.BLOCK_ID));
        de.watched = DownloadEntry.WatchedState.values()[c.getInt(c.getColumnIndex(DbStructure.Column.WATCHED))];
        de.eid = c.getString(c.getColumnIndex(DbStructure.Column.EID));
        de.chapter = c.getString(c.getColumnIndex(DbStructure.Column.CHAPTER));
        de.section = c.getString(c.getColumnIndex(DbStructure.Column.SECTION));
        de.downloadedOn = c.getLong(c.getColumnIndex(DbStructure.Column.DOWNLOADED_ON));
        de.lastPlayedOffset = c.getInt(c.getColumnIndex(DbStructure.Column.LAST_PLAYED_OFFSET));
        de.isCourseActive = c.getInt(c.getColumnIndex(DbStructure.Column.IS_COURSE_ACTIVE));
        de.isVideoForWebOnly = c.getInt(c.getColumnIndex(DbStructure.Column.VIDEO_FOR_WEB_ONLY)) == 1;
        de.lmsUrl = c.getString(c.getColumnIndex(DbStructure.Column.UNIT_URL));

        return de;
    }

    /**
     * Returns an object of IVideoModel which has all the fields copied from given VideoResponseModel.
     *
     * @param vrm
     * @return
     */
    public static VideoModel getModel(VideoResponseModel vrm) {
        DownloadEntry e = new DownloadEntry();
        e.chapter = vrm.getChapterName();
        e.section = vrm.getSequentialName();
        e.eid = vrm.getCourseId();
        e.duration = vrm.getSummary().getDuration();
        e.size = vrm.getSummary().getSize();
        e.title = vrm.getSummary().getDisplayName();
        e.url = vrm.getSummary().getVideoUrl();
        e.url_high_quality = vrm.getSummary().getHighEncoding();
        e.url_low_quality = vrm.getSummary().getLowEncoding();
        e.url_youtube = vrm.getSummary().getYoutubeLink();
        e.blockId = vrm.getSummary().getId();
        e.transcript = vrm.getSummary().getTranscripts();
        e.lmsUrl = vrm.getUnitUrl();

        e.isVideoForWebOnly = vrm.getSummary().isOnlyOnWeb();
        return e;
    }

    /**
     * Returns an object of IVideoModel which has all the fields copied from given VideoData.
     *
     * @param vrm
     * @return
     */
    public static VideoModel getModel(VideoData vrm, VideoBlockModel block) {
        DownloadEntry e = new DownloadEntry();
        //FIXME - current database schema is not suitable for arbitary level of course structure tree
        //solution - store the navigation path info in into one column field in the database,
        //rather than individual column fields.
        BlockPath path = block.getPath();
        e.chapter = path.get(1) == null ? "" : path.get(1).getDisplayName();
        e.section = path.get(2) == null ? "" : path.get(2).getDisplayName();
        IBlock root = block.getRoot();
        e.eid = root.getCourseId();
        e.duration = vrm.duration;
        final VideoInfo preferredVideoInfo = vrm.encodedVideos.getPreferredVideoInfo();
        e.size = preferredVideoInfo.fileSize;
        e.title = block.getDisplayName();
        e.url = preferredVideoInfo.url;
        e.url_high_quality = getVideoNetworkUrlOrNull(vrm.encodedVideos.mobileHigh);
        e.url_low_quality = getVideoNetworkUrlOrNull(vrm.encodedVideos.mobileLow);
        e.url_youtube = getVideoNetworkUrlOrNull(vrm.encodedVideos.youtube);
        e.blockId = block.getId();
        e.transcript = vrm.transcripts;
        e.lmsUrl = block.getBlockUrl();
        e.isVideoForWebOnly = vrm.onlyOnWeb;
        return e;
    }

    @Nullable
    private static String getVideoNetworkUrlOrNull(@Nullable VideoInfo videoInfo) {
        return videoInfo != null && URLUtil.isNetworkUrl(videoInfo.url) ? videoInfo.url : null;
    }

    public static AudioModel getModel(AudioData data, AudioBlockModel block) {
        DownloadEntry e = new DownloadEntry();
        //FIXME - current database schema is not suitable for arbitary level of course structure tree
        //solution - store the navigation path info in into one column field in the database,
        //rather than individual column fields.
        BlockPath path = block.getPath();
        e.chapter = path.get(1) == null ? "" : path.get(1).getDisplayName();
        e.section = path.get(2) == null ? "" : path.get(2).getDisplayName();
        IBlock root = block.getRoot();
        e.eid = root.getCourseId();
        final String preferredVideoInfo = data.encodedAudios.getPreferredPlaybackUrl();
        e.title = block.getDisplayName();
        e.url_mp3 = getVideoNetworkUrlOrNull(data.encodedAudios.mp3Url);
        e.url_ogg = getVideoNetworkUrlOrNull(data.encodedAudios.oggUrl);
        e.blockId = block.getId();
        e.transcript = data.getTranscripts();
        e.lmsUrl = block.getBlockUrl();
        return e;
    }

    private static String getVideoNetworkUrlOrNull(String url) {
        return URLUtil.isNetworkUrl(url) ? url : null;
    }
}
