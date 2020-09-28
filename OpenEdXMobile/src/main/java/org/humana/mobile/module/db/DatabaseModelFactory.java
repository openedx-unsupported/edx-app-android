package org.humana.mobile.module.db;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import org.humana.mobile.model.VideoModel;
import org.humana.mobile.model.api.VideoResponseModel;
import org.humana.mobile.model.course.BlockPath;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.model.course.IBlock;
import org.humana.mobile.model.course.VideoBlockModel;
import org.humana.mobile.model.course.VideoData;
import org.humana.mobile.model.course.VideoInfo;
import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.tta.analytics.AnalyticModel;
import org.humana.mobile.tta.analytics.analytics_enums.Source;
import org.humana.mobile.tta.data.enums.DownloadType;
import org.humana.mobile.tta.scorm.ScormBlockModel;
import org.humana.mobile.tta.scorm.ScormData;
import org.humana.mobile.tta.tincan.model.Resume;

/**
 * Model Factory class for the database models.
 *
 * @author rohan
 */
public class DatabaseModelFactory {

    /**
     * Returns new instance of {@link org.humana.mobile.model.VideoModel} initialized with given cursor.
     *
     * @param c
     * @return
     */
    public static VideoModel getModel(Cursor c) {
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
        de.url_hls = c.getString(c.getColumnIndex(DbStructure.Column.URL_HLS));
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
        de.isVideoForWebOnly = c.getInt(c.getColumnIndex(DbStructure.Column.VIDEO_FOR_WEB_ONLY)) == 1;
        de.lmsUrl = c.getString(c.getColumnIndex(DbStructure.Column.UNIT_URL));
        de.type = c.getString(c.getColumnIndex(DbStructure.Column.TYPE));
        de.content_id = c.getLong(c.getColumnIndex(DbStructure.Column.CONTENT_ID));
        de.scormUploadedOn = c.getString(c.getColumnIndex(DbStructure.Column.SCORM_UPLOADED_ON));
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
        e.videoId = vrm.getSummary().getId();
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
        final VideoInfo preferredVideoInfo = vrm.encodedVideos.getPreferredVideoInfoForDownloading();
        e.size = preferredVideoInfo.fileSize;
        e.title = block.getDisplayName();
        e.url = preferredVideoInfo.url;
        e.url_hls = getVideoNetworkUrlOrNull(vrm.encodedVideos.hls);
        e.url_high_quality = getVideoNetworkUrlOrNull(vrm.encodedVideos.mobileHigh);
        e.url_low_quality = getVideoNetworkUrlOrNull(vrm.encodedVideos.mobileLow);
        e.url_youtube = getVideoNetworkUrlOrNull(vrm.encodedVideos.youtube);
        e.videoId = block.getId();
        e.transcript = vrm.transcripts;
        e.lmsUrl = block.getBlockUrl();
        e.isVideoForWebOnly = vrm.onlyOnWeb;
        e.type = DownloadType.EDX_VIDEO.name();
        return e;
    }

    public static VideoModel getModel(ScormData scormData, ScormBlockModel block) {
        DownloadEntry e = new DownloadEntry();
        //FIXME - current database schema is not suitable for arbitary level of course structure tree
        //solution - store the navigation path info in into one column field in the database,
        //rather than individual column fields.
        BlockPath path = block.getPath();
        e.chapter = path.get(1) == null ? "" : path.get(1).getDisplayName();
        e.section = path.get(2) == null ? "" : path.get(2).getDisplayName();
        IBlock root = block.getRoot();
        e.eid = root.getCourseId();
//        e.duration = scormData.duration;
//        final VideoInfo preferredVideoInfo = scormData.encodedVideos.getPreferredVideoInfoForDownloading();
//        e.size = preferredVideoInfo.fileSize;
        e.title = block.getDisplayName();
//        e.url = preferredVideoInfo.url;
//        e.url_hls = getVideoNetworkUrlOrNull(scormData.encodedVideos.hls);
//        e.url_high_quality = getVideoNetworkUrlOrNull(scormData.encodedVideos.mobileHigh);
//        e.url_low_quality = getVideoNetworkUrlOrNull(scormData.encodedVideos.mobileLow);
//        e.url_youtube = getVideoNetworkUrlOrNull(scormData.encodedVideos.youtube);
        e.videoId = block.getId();
//        e.transcript = scormData.transcripts;
        e.lmsUrl = block.getBlockUrl();
//        e.isVideoForWebOnly = scormData.onlyOnWeb;
        if (block.getType().equals(BlockType.SCORM)) {
            e.type = DownloadType.SCORM.name();
        } else if (block.getType().equals(BlockType.PDF)) {
            e.type = DownloadType.PDF.name();
        }
        return e;
    }

    @Nullable
    private static String getVideoNetworkUrlOrNull(@Nullable VideoInfo videoInfo) {
        return videoInfo != null && URLUtil.isNetworkUrl(videoInfo.url) ? videoInfo.url : null;
    }

    /**
     * Returns new instance of {//@link org.tta.mobile.mx_analytic.org.tta.mobile.mx_analytic.AnalyticModel} initialized with given cursor.
     *
     * @param c
     * @return
     */
    public static AnalyticModel getAnalyticModel(Cursor c) {
        AnalyticModel ae = new AnalyticModel();

        ae.analytic_id =String.valueOf(c.getLong(c.getColumnIndex(DbStructure.Column.ANALYTIC_TB_ID)));
        ae.user_id=c.getString(c.getColumnIndex(DbStructure.Column.USER_ID));
        ae.action=c.getString(c.getColumnIndex(DbStructure.Column.ACTION));
        ae.metadata=c.getString(c.getColumnIndex(DbStructure.Column.METADATA));
        ae.source= String.valueOf(Source.Mobile);
        ae.page=c.getString(c.getColumnIndex(DbStructure.Column.PAGE));
        ae.setStatus(c.getInt(c.getColumnIndex(DbStructure.Column.STATUS)));
        ae.event_timestamp =c.getLong(c.getColumnIndex(DbStructure.Column.EVENT_DATE));
        ae.nav=c.getString(c.getColumnIndex(DbStructure.Column.NAV));
        ae.action_id=c.getString(c.getColumnIndex(DbStructure.Column.ACTION_ID));

        return ae;
    }

    /**
     * Returns new instance of {//@link org.tta.mobile.mx_analytic.org.tta.mobile.tincan.model.Resume} initialized with given cursor.
     *
     * @param c
     * @return resume
     */
    public static Resume getResumeModel(Cursor c) {
        Resume resume = new Resume();

        resume.setId(String.valueOf(c.getLong(c.getColumnIndex(DbStructure.Column.ID))));
        resume.setUser_Id(c.getString(c.getColumnIndex(DbStructure.Column.USER_ID)));
        resume.setCourse_Id(c.getString(c.getColumnIndex(DbStructure.Column.COURSE_ID)));
        resume.setUnit_id(c.getString(c.getColumnIndex(DbStructure.Column.UNIT_ID)));
        resume.setResume_Payload(c.getString(c.getColumnIndex(DbStructure.Column.RESUME_PAYLOAD)));

        return resume;
    }
}
