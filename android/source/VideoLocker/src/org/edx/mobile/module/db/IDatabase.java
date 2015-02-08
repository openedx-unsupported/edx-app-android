package org.edx.mobile.module.db;

import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.db.DownloadEntry.DownloadedState;
import org.edx.mobile.model.db.DownloadEntry.WatchedState;
import org.edx.mobile.module.db.impl.DatabaseFactory;

import java.util.List;

/**
 * This interface represents a database object. All the methods that are required by UI layer 
 * are declared in this interface. {@link DatabaseFactory} class provides instance 
 * of the interface implementation which is used by UI layer.
 * 
 * @author rohan
 *
 */
public interface IDatabase {
    
    /**
     * Releases this database object and all its handles.
     */
    void release();

    /**
     * Return true if any Video is marked as Downloading in the database for logged in user
     * Used to display the top downloading icon 
     * @return boolean flag if download is in progress
     */
    public Boolean isAnyVideoDownloading(DataCallback<Boolean> callback);
    
    
    /**
     * Returns all dmid's which are currently being downloaded for logged in user
     * This method is used for showing progress in the DownloadModule
     * @return 
     */
    public List<Long> getAllDownloadingVideosDmidList(DataCallback<List<Long>> callback);
    
    
    /**
     * Mark all videos as course deactivated for logged in user 
     */
    public Integer updateAllVideosAsDeactivated(DataCallback<Integer> callback);
    
    /**
     * Mark all videos with enrollment id as course activated for logged in user
     * @param enrollmentId
     * @return The number of rows affected
     */
    public Integer updateVideosActivatedForCourse(String enrollmentId, DataCallback<Integer> callback);
    
    /**
     * Returns all Deactivated videos for logged in user
     * @param callback
     */
    public List<IVideoModel> getAllDeactivatedVideos(DataCallback<List<IVideoModel>> callback);
    
    
    /**
     * Mark the Video as online and reset the filepath and dmid for logged in user  
     * @param videoId  - IVideoModel object
     */
    public Integer updateVideoAsOnlineByVideoId(String videoId, DataCallback<Integer> callback);
    
    
    /**
     * Returns count of Videos with passed DMID for logged in user
     * @param callback
     */
    public Integer getVideoCountBydmId(long dmId, DataCallback<Integer> callback);
    
    
    /**
     * Returns true if Video is downloaded in Chapter for logged in user 
     * @param enrollmentId - course which has the chapter
     * @param chapter
     */
    public Boolean isVideoDownloadedInChapter(String enrollmentId, 
            String chapter, DataCallback<Boolean> callback);
    
    /**
     * Returns number of videos marked as downloading/downloaded in Chapter for logged in user 
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @return - Number of videos not online
     */
    public Integer getVideosCountByChapter(String enrollmentId, 
            String chapter, DataCallback<Integer> callback);
    
    
    /**
     * Returns true if any video downloading is in progress for chapter
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @return - Number of Downloaded videos
     */
    public Boolean isVideoDownloadingInChapter(String enrollmentId, 
            String chapter, DataCallback<Boolean> callback);
    
    
    /**
     * Returns dmId's of all downloading videos for given Chapter of logged in user 
     * @param enrollmentId - course which has the chapter
     * @param chapter
     */
    public List<Long> getDownloadingVideoDmIdsForChapter(String enrollmentId, String chapter, 
            DataCallback<List<Long>> callback);
    
    
    /**
     * Returns true if any video downloading is in progress for Section
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @param section
     */
    public Boolean isVideoDownloadingInSection(String enrollmentId, String chapter,
            String section, DataCallback<Boolean> callback);
    
    
    /**
     * Returns dmId's of all downloading videos for given section of logged in user 
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @param section
     */
    public List<Long> getDownloadingVideoDmIdsForSection(String enrollmentId, String chapter, 
            String section, final DataCallback<List<Long>> callback);
    
    
    /**
     * Returns number of videos marked as downloading/downloaded in Section for logged in user 
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @param section
     */
    public Integer getVideosCountBySection(String enrollmentId, String chapter, 
            String section, DataCallback<Integer> callback);
    
    /**
     * Update a Video's watched state 
     * @param videoId  - Id of video for which status needs to change
     * @param state - Status flag to be set for changing Video watched state
     */
    public Integer updateVideoWatchedState(String videoId, WatchedState state,
            DataCallback<Integer> callback);
    
    
    /**
     * Update a Video's last watched time 
     * @param videoId  - Id of video for which status needs to change
     * @param offset - Last Played offset 
     */
    public Integer updateVideoLastPlayedOffset(String videoId, int offset,
            DataCallback<Integer> callback);
    
    
    /**
     * Insert Download Entry in the database 
     * @param de  - IVideoModel object
     * @param callback
     * @return - the row ID of the newly inserted row, or -1 if an error occurred
     */
    public Long addVideoData(IVideoModel de, DataCallback<Long> callback);
    
    /**
     * Returns VideoEntry for the passed VideoId
     * @param videoId
     * @param callback
     */
    public IVideoModel getVideoEntryByVideoId(String videoId, DataCallback<IVideoModel> callback);
    
    /**
     * Returns {@link IVideoModel} for given videoUrl.
     * @param videoUrl
     * @param callback
     * @return
     */
    IVideoModel getVideoByVideoUrl(String videoUrl, DataCallback<IVideoModel> callback);
    
    
    /**
     * Marks given Video as online and sets dmid to -1 so that this video is identified as NOT_DOWNLAODED.
     * File path for this video is made empty so as to avoid access to non-existing file. 
     * NOTE: If there are multiple videos with same URL that are marked as Downloaded,
     * Only the reference should be removed and not the downloaded file
     * @param de  - IVideoModel object
     * @param callback
     * @return - the row ID of the newly inserted row, or -1 if an error occurred
     */
    Integer deleteVideoByVideoId(IVideoModel de, DataCallback<Integer> callback);
    
    
    /**
     * Returns if a IVideoModel with the same video URL is downloaded
     * This method is used to display the progress if video is already downloaded
     * @param url
     */
    public Boolean isVideoFilePresentByUrl(String url, DataCallback<Boolean> callback);
    
    
    /**
     * This method updates info for Videos with the same URL and have been enqueued for downloading
     * @param model
     * @param callback
     */
    public Integer updateDownloadingVideoInfoByVideoId(IVideoModel model, 
            DataCallback<Integer> callback);
    
    /**
     * This method marks the Video as Downloading when enqueued for Download
     * @param model
     * @param callback
     */
    public Integer updateAsDownloadingByVideoId(IVideoModel model, 
            DataCallback<Integer> callback);
    
    /**
     * Returns list of All VideoEntries which are currently being downloaded
     * @return
     */
    public List<IVideoModel> getListOfOngoingDownloads(DataCallback<List<IVideoModel>> callback);
    
    
    /**
     * Returns no of Videos which have been completely 
     * downloaded and marked as Downloaded in the DB
     * @return - count of Videos downloaded
     */
    public Integer getVideosDownloadedCount(DataCallback<Integer> callback);
    
    /**
     * Returns Count of number of Downloaded Videos in the Course by Course ID 
     * @param courseId
     * @param callback
     */
    public Integer getDownloadedVideoCountByCourse(String courseId, DataCallback<Integer> callback);
    
    
    /**
     * Returns Downloaded Videos in the Course by Course ID 
     * @param courseId
     * @param callback
     */
    public List<IVideoModel> getDownloadedVideoListForCourse(String courseId, 
            DataCallback<List<IVideoModel>> callback);
    
    
    /**
     * Returns Size in bytes of Downloaded Videos in the Course by Course ID 
     * @param courseId
     * @param callback
     */
    public Long getDownloadedVideosSizeByCourse(String courseId, DataCallback<Long> callback);
    
    /**
     * Returns IVideoModel object if entry exists with Video status set as 
     * downloaded with the given URL
     */
    public IVideoModel getIVideoModelByVideoUrl(String videoUrl,
            DataCallback<IVideoModel> callback);
    
    
    /**
     * Return true if IVideoModel for associated dmId is present in db. Do not use username 
     * as this function is to check if the dmid is of VideoLocker or other application
     * @param dmId
     * @return
     */
    public Boolean isDmIdExists(long dmId, DataCallback<Boolean> callback);
    
    /**
     * Marks the download as complete for the given dmid. 
     * NOTE - This should be done irrespective of username as if the user is 
     * logged out and download is in progress, it should update download complete in the db.
     * @param dmId
     * @return
     */
    public Integer updateDownloadCompleteInfoByDmId(long dmId, 
            IVideoModel de, DataCallback<Integer> callback);

    /**
     * Returns list of all videos from the database.
     * @return
     */
    List<IVideoModel> getAllVideos(String username, DataCallback<List<IVideoModel>> DataCallback);

    /**
     * Removes all records of given username from the datbase.
     * @param username
     */
    void clearDataByUser(String username);

    /**
     * This method gives the WatchedState stored in the DB for VideoID
     * @param videoId
     * @param dataCallback
     */
    WatchedState getWatchedStateForVideoId(String videoId,
            DataCallback<WatchedState> dataCallback);

    /**
     * Returns count of videos which have given URL as their video URL.
     * @param videoUrl
     * @param callback
     * @return
     */
    Integer getVideoCountByVideoUrl(String videoUrl, DataCallback<Integer> callback);
    
    /**
     * Returns count of videos which have given URL as their video URL.
     * @param dmId
     * @param callback
     * @return
     */
    IVideoModel getDownloadEntryByDmId(long dmId, DataCallback<IVideoModel> callback);
    
    /**
     * This function is used to getting all sorted Downloads based on Download date
     */
    public List<IVideoModel> getSortedDownloadsByDownloadedDateForCourseId(String courseId,
            DataCallback<List<IVideoModel>> callback);
    
    /**
     * This method gives the WatchedState stored in the DB for VideoID
     * @param videoId
     * @param dataCallback
     */
    DownloadedState getDownloadedStateForVideoId(String videoId,
            DataCallback<DownloadedState> dataCallback);
    
}
