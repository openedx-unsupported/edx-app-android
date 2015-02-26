package org.edx.mobile.module.db.impl;

import android.content.ContentValues;
import android.content.Context;
import java.util.List;

import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.db.DownloadEntry.DownloadedState;
import org.edx.mobile.model.db.DownloadEntry.WatchedState;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.DbStructure;
import org.edx.mobile.module.db.IDatabase;

class IDatabaseImpl extends IDatabaseBaseImpl implements IDatabase {

    private String username;

    public IDatabaseImpl(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    public Boolean isAnyVideoDownloading(final DataCallback<Boolean> callback) {
        DbOperationExists op = new DbOperationExists(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.USERNAME + "=? AND "+ DbStructure.Column.DOWNLOADED + "=?", 
                new String[] { username, String.valueOf(DownloadedState.DOWNLOADING.ordinal()) }, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public List<Long> getAllDownloadingVideosDmidList(final DataCallback<List<Long>> callback){
        DbOperationGetColumn<Long> op = new DbOperationGetColumn<Long>(true,DbStructure.Table.DOWNLOADS, new String[]{DbStructure.Column.DM_ID},
                DbStructure.Column.DOWNLOADED + "=? AND " + DbStructure.Column.USERNAME + "=?",
                new String[] {String.valueOf(DownloadedState.DOWNLOADING.ordinal()), username}, null, Long.class);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Integer updateAllVideosAsDeactivated(final DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.IS_COURSE_ACTIVE, false);

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.USERNAME + "=?", new String[] {username});
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Integer updateVideosActivatedForCourse(String enrollmentId, final DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.IS_COURSE_ACTIVE, true);

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.EID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] {enrollmentId, username});
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public List<IVideoModel> getAllDeactivatedVideos(final DataCallback<List<IVideoModel>> callback) {
        DbOperationGetVideos op = new DbOperationGetVideos(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.IS_COURSE_ACTIVE + "=? AND "+ DbStructure.Column.USERNAME + "=? " , 
                new String[] { "0", username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Integer updateVideoAsOnlineByVideoId(String videoId, final DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.DM_ID, 0);
        values.put(DbStructure.Column.FILEPATH, "");
        values.put(DbStructure.Column.DOWNLOADED, DownloadedState.ONLINE.ordinal());

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.VIDEO_ID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] {videoId, username});
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Integer getVideoCountBydmId(long dmId, final DataCallback<Integer> callback) {
        DbOperationGetCount op = new DbOperationGetCount(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.DM_ID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] { String.valueOf(dmId), username }, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Boolean isVideoDownloadedInChapter(String enrollmentId,
            String chapter, final DataCallback<Boolean> callback) {
        DbOperationExists op = new DbOperationExists(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.CHAPTER + "=? AND " + DbStructure.Column.EID + "=? AND " 
                        + DbStructure.Column.DOWNLOADED + "=? AND " + DbStructure.Column.USERNAME + "=?",
                        new String[] { chapter, enrollmentId, 
                String.valueOf(DownloadedState.DOWNLOADED.ordinal()),username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Integer getVideosCountByChapter(String enrollmentId, String chapter, 
            final DataCallback<Integer> callback) {
        DbOperationGetCount op = new DbOperationGetCount(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.CHAPTER + "=? AND " + DbStructure.Column.EID + "=? AND " 
                        + DbStructure.Column.DOWNLOADED + "!=? AND " + DbStructure.Column.USERNAME + "=?",
                        new String[] { chapter, enrollmentId, 
                String.valueOf(DownloadedState.ONLINE.ordinal()),username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Boolean isVideoDownloadingInChapter(String enrollmentId,
            String chapter, final DataCallback<Boolean> callback) {
        DbOperationExists op = new DbOperationExists(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.CHAPTER + "=? AND " + DbStructure.Column.EID + "=? AND " 
                        + DbStructure.Column.DOWNLOADED + "=? AND " + DbStructure.Column.USERNAME + "=?",
                        new String[] { chapter, enrollmentId, 
                String.valueOf(DownloadedState.DOWNLOADING.ordinal()),username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public List<Long> getDownloadingVideoDmIdsForChapter(String enrollmentId,
            String chapter, final DataCallback<List<Long>> callback) {
        DbOperationGetColumn<Long> op = new DbOperationGetColumn<Long>(true,DbStructure.Table.DOWNLOADS, 
                new String[]{DbStructure.Column.DM_ID},
                DbStructure.Column.DOWNLOADED + "=? AND "+ DbStructure.Column.EID + "=? AND " 
                        + DbStructure.Column.CHAPTER + "=? AND "+ DbStructure.Column.USERNAME + "=?",
                        new String[] {String.valueOf(DownloadedState.DOWNLOADING.ordinal()), 
                enrollmentId, chapter,username}, null, Long.class);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Boolean isVideoDownloadingInSection(String enrollmentId,
            String chapter, String section, final DataCallback<Boolean> callback) {
        DbOperationExists op = new DbOperationExists(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.SECTION + "=? AND " + DbStructure.Column.CHAPTER + "=? AND " 
                        + DbStructure.Column.EID + "=? AND "+ DbStructure.Column.DOWNLOADED + "=? AND "
                        + DbStructure.Column.USERNAME + "=?",
                        new String[] {section, chapter, enrollmentId, 
                String.valueOf(DownloadedState.DOWNLOADING.ordinal()),username}, null);
        op.setCallback(callback);
        return enqueue(op);

    }

    @Override
    public List<Long> getDownloadingVideoDmIdsForSection(String enrollmentId, String chapter, 
            String section , final DataCallback<List<Long>> callback ) {
        DbOperationGetColumn<Long> op = new DbOperationGetColumn<Long>(true,DbStructure.Table.DOWNLOADS, 
                new String[]{DbStructure.Column.DM_ID},
                DbStructure.Column.DOWNLOADED + "=? AND "+ DbStructure.Column.EID + "=? AND " 
                        + DbStructure.Column.CHAPTER + "=? AND "
                        + DbStructure.Column.SECTION + "=? AND "
                        + DbStructure.Column.USERNAME + "=?",
                        new String[] {String.valueOf(DownloadedState.DOWNLOADING.ordinal()), 
                enrollmentId, chapter, section, username}, null, Long.class);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Integer getVideosCountBySection(String enrollmentId, String chapter,
            String section, final DataCallback<Integer> callback) {
        DbOperationGetCount op = new DbOperationGetCount(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.SECTION + "=? AND "+DbStructure.Column.CHAPTER + "=? AND " + DbStructure.Column.EID + "=? AND " 
                        + DbStructure.Column.DOWNLOADED + "!=? AND " + DbStructure.Column.USERNAME + "=?",
                        new String[] { section, chapter, enrollmentId, 
                String.valueOf(DownloadedState.ONLINE.ordinal()),username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Integer updateVideoWatchedState(String videoId, WatchedState status, 
            final DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.WATCHED, status.ordinal());

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.VIDEO_ID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] {videoId, username});
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Integer updateVideoLastPlayedOffset(String videoId, int offset,
            final DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.LAST_PLAYED_OFFSET, offset);

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.VIDEO_ID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] {videoId, username});
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Long addVideoData(final IVideoModel de, final DataCallback<Long> callback) {
        IVideoModel result = getVideoEntryByVideoId(de.getVideoId(), null);
        if (result == null) {
            ContentValues values = new ContentValues();
            values.put(DbStructure.Column.USERNAME, username);
            values.put(DbStructure.Column.TITLE, de.getTitle());
            values.put(DbStructure.Column.VIDEO_ID, de.getVideoId());
            values.put(DbStructure.Column.SIZE, de.getSize());
            values.put(DbStructure.Column.DURATION, de.getDuration());
            values.put(DbStructure.Column.FILEPATH, de.getFilePath());
            values.put(DbStructure.Column.URL, de.getVideoUrl());
            values.put(DbStructure.Column.URL_HIGH_QUALITY, de.getHighQualityVideoUrl());
            values.put(DbStructure.Column.URL_LOW_QUALITY, de.getLowQualityVideoUrl());
            values.put(DbStructure.Column.URL_YOUTUBE, de.getYoutubeVideoUrl());
            values.put(DbStructure.Column.WATCHED, de.getWatchedStateOrdinal());
            values.put(DbStructure.Column.DOWNLOADED, de.getDownloadedStateOrdinal());
            values.put(DbStructure.Column.DM_ID, de.getDmId());
            values.put(DbStructure.Column.EID, de.getEnrollmentId());
            values.put(DbStructure.Column.CHAPTER, de.getChapterName());
            values.put(DbStructure.Column.SECTION, de.getSectionName());
            values.put(DbStructure.Column.LAST_PLAYED_OFFSET, de.getLastPlayedOffset());
            values.put(DbStructure.Column.UNIT_URL, de.getLmsUrl());
            values.put(DbStructure.Column.IS_COURSE_ACTIVE, de.isCourseActive());

            DbOperationInsert op = new DbOperationInsert(DbStructure.Table.DOWNLOADS, values);
            op.setCallback(callback);
            return enqueue(op);
        } else {
            if(callback!=null){
                callback.sendResult(0L);
            }
            logger.warn("Not inserting, this seems a duplicate record");
        }

        return 0L;
    }


    /**
     * Returns download entry for given video id.
     * @param videoId
     * @return
     */
    public IVideoModel getVideoEntryByVideoId(String videoId, final DataCallback<IVideoModel> callback) {
        DbOperationGetVideo op = new DbOperationGetVideo(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.VIDEO_ID + "=? AND "+ DbStructure.Column.USERNAME + "=?" , 
                new String[] { videoId, username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public IVideoModel getVideoByVideoUrl(String videoUrl,
            DataCallback<IVideoModel> callback) {
        DbOperationGetVideo op = new DbOperationGetVideo(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.URL + "=? AND "+ DbStructure.Column.USERNAME + "=?" , 
                new String[] { videoUrl, username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Integer deleteVideoByVideoId(IVideoModel video, DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.DOWNLOADED, DownloadedState.ONLINE.ordinal());
        values.put(DbStructure.Column.DM_ID, -1);
        values.put(DbStructure.Column.FILEPATH, "");

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.VIDEO_ID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] { video.getVideoId(), username});
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Boolean isVideoFilePresentByUrl(String videoUrl, final DataCallback<Boolean> callback) {
        DbOperationExists op = new DbOperationExists(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.URL + "=? AND "+ DbStructure.Column.DOWNLOADED + "=? AND " 
                        + DbStructure.Column.USERNAME + "=?", 
                        new String[] { videoUrl,String.valueOf(DownloadedState.DOWNLOADED.ordinal()),
                username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Integer updateDownloadingVideoInfoByVideoId(IVideoModel model, 
            DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.DM_ID, model.getDmId());
        values.put(DbStructure.Column.DOWNLOADED, model.getDownloadedStateOrdinal());
        values.put(DbStructure.Column.DURATION, model.getDuration());
        values.put(DbStructure.Column.FILEPATH, model.getFilePath());
        values.put(DbStructure.Column.SIZE, model.getSize());
        values.put(DbStructure.Column.IS_COURSE_ACTIVE, model.isCourseActive());

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.VIDEO_ID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] { model.getVideoId(),username});
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Integer updateAsDownloadingByVideoId(IVideoModel model, 
            DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.DM_ID, model.getDmId());
        values.put(DbStructure.Column.DOWNLOADED, DownloadedState.DOWNLOADING.ordinal());

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.VIDEO_ID + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] { model.getVideoId(),username});
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public List<IVideoModel> getListOfOngoingDownloads(final DataCallback<List<IVideoModel>> callback) {
        DbOperationGetVideos op = new DbOperationGetVideos(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.DOWNLOADED + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] {String.valueOf(DownloadedState.DOWNLOADING.ordinal()), username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public Integer getVideosDownloadedCount(final DataCallback<Integer> callback) {
        DbOperationGetCount op = new DbOperationGetCount(false,DbStructure.Table.DOWNLOADS, 
                null, 
                DbStructure.Column.DOWNLOADED + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] {String.valueOf(DownloadedState.DOWNLOADED.ordinal()), username}, null);
        op.setCallback(callback);
        return enqueue(op);

    }

    @Override
    public Integer getDownloadedVideoCountByCourse(String courseId, final DataCallback<Integer> callback){
        DbOperationGetCount op = new DbOperationGetCount(true,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.EID + "=? AND " + DbStructure.Column.DOWNLOADED + "=? AND " + DbStructure.Column.USERNAME + "=?",
                new String[] { courseId, String.valueOf(DownloadedState.DOWNLOADED.ordinal()), username },
                null);
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public List<IVideoModel> getDownloadedVideoListForCourse(String courseId, final DataCallback<List<IVideoModel>> callback){
        DbOperationGetVideos op = new DbOperationGetVideos(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.EID + "=? AND " + DbStructure.Column.DOWNLOADED + "=? AND " 
                        + DbStructure.Column.USERNAME + "=?", 
                        new String[] {courseId, String.valueOf(DownloadedState.DOWNLOADED.ordinal()),
                username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Long getDownloadedVideosSizeByCourse(String courseId, final DataCallback<Long> callback){
        String sqlQuery = "SELECT SUM(" + DbStructure.Column.SIZE + ") FROM "
                + DbStructure.Table.DOWNLOADS + " WHERE " 
                + DbStructure.Column.EID + "=? AND "
                + DbStructure.Column.USERNAME + "=? AND " 
                + DbStructure.Column.DOWNLOADED + "=?";
        DbOperationSingleValueByRawQuery<Long> op = new DbOperationSingleValueByRawQuery<Long>(
                sqlQuery,
                new String[] { courseId, username, String.valueOf(DownloadedState.DOWNLOADED.ordinal()) }, 
                Long.class);
        op.setCallback(callback);
        return enqueue(op);
    }


    @Override
    public List<IVideoModel> getSortedDownloadsByDownloadedDateForCourseId(String courseId,
            DataCallback<List<IVideoModel>> callback) {
        DbOperationGetVideos op = new DbOperationGetVideos(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.EID + "=? AND " + DbStructure.Column.DOWNLOADED + "=? AND " 
                        + DbStructure.Column.USERNAME + "=?", 
                        new String[] { courseId, String.valueOf(
                                DownloadedState.DOWNLOADED.ordinal()), username }, 
                                DbStructure.Column.DOWNLOADED_ON + " DESC");
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public IVideoModel getIVideoModelByVideoUrl(String videoUrl,
            final DataCallback<IVideoModel> callback) {
        DbOperationGetVideo op = new DbOperationGetVideo(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.URL + "=? AND "+ DbStructure.Column.USERNAME + "=?" , 
                new String[] { videoUrl, username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Boolean isDmIdExists(long dmId, final DataCallback<Boolean> callback) {
        DbOperationExists op = new DbOperationExists(false, DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.USERNAME + "=? AND "+ DbStructure.Column.DM_ID + "=?", 
                new String[] { username, String.valueOf(dmId) }, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public Integer updateDownloadCompleteInfoByDmId(long dmId, 
            IVideoModel model, DataCallback<Integer> callback) {
        ContentValues values = new ContentValues();
        values.put(DbStructure.Column.SIZE, model.getSize());
        values.put(DbStructure.Column.DURATION, model.getDuration());
        values.put(DbStructure.Column.FILEPATH, model.getFilePath());
        values.put(DbStructure.Column.URL, model.getVideoUrl());
        values.put(DbStructure.Column.URL_HIGH_QUALITY, model.getHighQualityVideoUrl());
        values.put(DbStructure.Column.URL_LOW_QUALITY, model.getLowQualityVideoUrl());
        values.put(DbStructure.Column.URL_YOUTUBE, model.getYoutubeVideoUrl());
        values.put(DbStructure.Column.DOWNLOADED, model.getDownloadedStateOrdinal());
        values.put(DbStructure.Column.DOWNLOADED_ON, model.getDownloadedOn());

        DbOperationUpdate op = new DbOperationUpdate(DbStructure.Table.DOWNLOADS, values,
                DbStructure.Column.DM_ID + "=? AND " + DbStructure.Column.DOWNLOADED + "!=?", 
                new String[] { String.valueOf(dmId), 
                String.valueOf(DownloadedState.ONLINE.ordinal())});
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public List<IVideoModel> getAllVideos(String username, final DataCallback<List<IVideoModel>> callback) {
        DbOperationGetVideos op = new DbOperationGetVideos(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.USERNAME + "=?", new String[] { username }, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public void clearDataByUser(String username) {
        DbOperationDelete op = new DbOperationDelete(DbStructure.Table.DOWNLOADS, 
                DbStructure.Column.USERNAME + "=?", 
                new String[] { username } );
        enqueue(op);
    }

    @Override
    public void release() {
        super.release();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public WatchedState getWatchedStateForVideoId(String videoId,
            final DataCallback<WatchedState> dataCallback) {
        DbOperationGetColumn<Integer> op = new DbOperationGetColumn<Integer>(false,DbStructure.Table.DOWNLOADS, 
                new String[] { DbStructure.Column.WATCHED }, 
                DbStructure.Column.VIDEO_ID + "=? AND "+ DbStructure.Column.USERNAME + "=?" , 
                new String[] { videoId, username}, null, Integer.class);
        op.setCallback(new DataCallback<List<Integer>>() {
            @Override
            public void onResult(List<Integer> ordinals) {
                if(ordinals!=null && !ordinals.isEmpty()){
                    dataCallback.sendResult(WatchedState.values()[ordinals.get(0)]);
                }else{
                    dataCallback.sendResult(WatchedState.UNWATCHED);
                }
            }

            @Override
            public void onFail(Exception ex) {
                dataCallback.sendException(ex);
            }
        });
        return enqueue(op);
    }

    @Override
    public Integer getVideoCountByVideoUrl(String videoUrl, DataCallback<Integer> callback) {
        DbOperationGetCount op = new DbOperationGetCount(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.URL + "=? AND " + DbStructure.Column.USERNAME + "=?", 
                new String[] { videoUrl, username }, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    @Override
    public IVideoModel getDownloadEntryByDmId(long dmId,
            DataCallback<IVideoModel> callback) {
        DbOperationGetVideo op = new DbOperationGetVideo(false,DbStructure.Table.DOWNLOADS, null, 
                DbStructure.Column.DM_ID + "=? AND "+ DbStructure.Column.DOWNLOADED + "=?" , 
                new String[] { String.valueOf(dmId), String.valueOf(DownloadedState
                        .DOWNLOADING.ordinal())}, null);
        op.setCallback(callback);
        return enqueue(op);
    }

    /*@Override
    public Boolean isVideoDownloadingByVideoId(String videoId,
            DataCallback<Boolean> callback) {
        DbOperationExists op = new DbOperationExists(false,DbStructure.Table.DOWNLOADS, 
                new String[] {DbStructure.Column.VIDEO_ID}, 
                DbStructure.Column.VIDEO_ID + "=? "
                        + DbStructure.Column.DOWNLOADED + "=? AND " + DbStructure.Column.USERNAME + "=?",
                        new String[] { videoId, String.valueOf(DownloadedState.DOWNLOADING.ordinal())
                ,username}, null);
        op.setCallback(callback);
        return enqueue(op);
    }*/
    
    @Override
    public DownloadedState getDownloadedStateForVideoId(String videoId,
            final DataCallback<DownloadedState> dataCallback) {
        DbOperationGetColumn<Integer> op = new DbOperationGetColumn<Integer>(false,DbStructure.Table.DOWNLOADS, 
                new String[] { DbStructure.Column.DOWNLOADED }, 
                DbStructure.Column.VIDEO_ID + "=? AND "+ DbStructure.Column.USERNAME + "=?" , 
                new String[] { videoId, username}, null, Integer.class);
        op.setCallback(new DataCallback<List<Integer>>() {
            @Override
            public void onResult(List<Integer> ordinals) {
                if(ordinals!=null && !ordinals.isEmpty()){
                    dataCallback.sendResult(DownloadedState.values()[ordinals.get(0)]);
                }else{
                    dataCallback.sendResult(DownloadedState.ONLINE);
                }
            }

            @Override
            public void onFail(Exception ex) {
                dataCallback.sendException(ex);
            }
        });
        return enqueue(op);
    }
}
