package org.edx.mobile.module.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.course.CourseComponent;
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
 */
public interface IDatabase {
    /**
     * Releases this database object and all its handles.
     */
    void release();

    /**
     * Return true if any Video is marked as Downloading in the database for logged in user
     * Used to display the top downloading icon
     *
     * @return boolean flag if download is in progress
     */
    Boolean isAnyVideoDownloading(DataCallback<Boolean> callback);


    /**
     * Returns all dmid's which are currently being downloaded for logged in user
     * This method is used for showing progress in the DownloadModule
     *
     * @return
     */
    List<Long> getAllDownloadingVideosDmidList(DataCallback<List<Long>> callback);


    /**
     * Mark all videos as course deactivated for logged in user
     */
    Integer updateAllVideosAsDeactivated(DataCallback<Integer> callback);

    /**
     * Mark all videos with enrollment id as course activated for logged in user
     *
     * @param enrollmentId
     * @return The number of rows affected
     */
    Integer updateVideosActivatedForCourse(String enrollmentId, DataCallback<Integer> callback);

    /**
     * Returns all Deactivated videos for logged in user
     *
     * @param callback
     */
    List<VideoModel> getAllDeactivatedVideos(DataCallback<List<VideoModel>> callback);


    /**
     * Mark the Video as online and reset the filepath and dmid for logged in user
     *
     * @param videoId - IVideoModel object
     */
    Integer updateVideoAsOnlineByVideoId(String videoId, DataCallback<Integer> callback);


    /**
     * Returns count of Videos with passed DMID for logged in user
     *
     * @param callback
     */
    Integer getVideoCountBydmId(long dmId, DataCallback<Integer> callback);


    /**
     * Returns true if Video is downloaded in Chapter for logged in user
     *
     * @param enrollmentId - course which has the chapter
     * @param chapter
     */
    Boolean isVideoDownloadedInChapter(String enrollmentId, String chapter,
                                       DataCallback<Boolean> callback);

    /**
     * Returns number of videos marked as downloading/downloaded in Chapter for logged in user
     *
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @return - Number of videos not online
     */
    Integer getVideosCountByChapter(String enrollmentId, String chapter,
                                    DataCallback<Integer> callback);

    /**
     * Return number of videos marked as web_view_only inChapter for logged in user
     *
     * @param enrollmentId
     * @param chapter
     * @param callback
     * @return
     */
    Integer getWebOnlyVideosCountByChapter(String enrollmentId, String chapter,
                                           final DataCallback<Integer> callback);

    /**
     * Returns true if any video downloading is in progress for chapter
     *
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @return - Number of Downloaded videos
     */
    Boolean isVideoDownloadingInChapter(String enrollmentId, String chapter,
                                        DataCallback<Boolean> callback);


    /**
     * Returns dmId's of all downloading videos for given Chapter of logged in user
     *
     * @param enrollmentId - course which has the chapter
     * @param chapter
     */
    List<Long> getDownloadingVideoDmIdsForChapter(String enrollmentId, String chapter,
                                                  DataCallback<List<Long>> callback);


    /**
     * Returns true if any video downloading is in progress for Section
     *
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @param section
     */
    Boolean isVideoDownloadingInSection(String enrollmentId, String chapter,
                                        String section, DataCallback<Boolean> callback);

    /**
     * Returns {@link android.app.DownloadManager} IDs of all downloading videos in a given section.
     *
     * @param enrollmentId course which has the chapter
     * @param chapter      the chapter
     * @param section      the section inside chapter
     * @param callback     callback to return results to
     * @return If the callback is null, returns an array containing the IDs for the downloading
     * videos, or an empty array if there are no videos downloading in the section. Otherwise,
     * returns null.
     */
    long[] getDownloadingVideoDmIdsForSection(String enrollmentId, String chapter, String section,
                                              final DataCallback<List<Long>> callback);

    /**
     * Returns the count of downloading videos for given section
     *
     * @param enrollmentId course which has the chapter
     * @param chapter      the chapter
     * @param section      the section inside chapter
     * @param callback     callback to return results to
     * @return Count of downloading videos for a given section
     */
    int getDownloadingVideosCountForSection(String enrollmentId, String chapter, String section,
                                            final DataCallback<Integer> callback);

    /**
     * Returns number of videos marked as downloading/downloaded in Section for logged in user
     *
     * @param enrollmentId - course which has the chapter
     * @param chapter
     * @param section
     */
    Integer getVideosCountBySection(String enrollmentId, String chapter,
                                    String section, DataCallback<Integer> callback);

    /**
     * Returns true if Video is downloaded in Section for logged in user
     */
    Boolean isVideoDownloadedInSection(String enrollmentId, String chapter,
                                       String section, DataCallback<Boolean> callback);

    /**
     * Returns dmId's of all downloaded videos for given section of logged in user
     *
     * @param enrollmentId course which has the chapter
     * @param chapter      the chapter
     * @param section      the section inside chapter
     * @param callback     callback to return results to
     * @return If the callback is null, returns an array containing the IDs for the downloaded
     * videos, or an empty array if there are no videos downloaded in the section. Otherwise,
     * returns null.
     */
    long[] getDownloadedVideoDmIdsForSection(String enrollmentId, String chapter, String section,
                                             final DataCallback<List<Long>> callback);

    /**
     * Returns the count of downloaded videos for given section
     *
     * @param enrollmentId course which has the chapter
     * @param chapter      the chapter
     * @param section      the section inside chapter
     * @param callback     callback to return results to
     * @return Count of downloaded videos for a given section
     */
    int getDownloadedVideosCountForSection(String enrollmentId, String chapter, String section,
                                           final DataCallback<Integer> callback);

    /**
     * get number of videos marked as webOnly
     *
     * @param enrollmentId
     * @param chapter
     * @param section
     * @param callback
     * @return
     */
    Integer getWebOnlyVideosCountBySection(String enrollmentId, String chapter, String section,
                                           final DataCallback<Integer> callback);

    /**
     * Update a Video's watched state
     *
     * @param videoId - Id of video for which status needs to change
     * @param state   - Status flag to be set for changing Video watched state
     */
    Integer updateVideoWatchedState(String videoId, WatchedState state,
                                    DataCallback<Integer> callback);


    /**
     * Update a Video's last watched time
     * @param videoId - Id of video for which status needs to change
     * @param offset  - Last Played offset
     */
    Integer updateVideoLastPlayedOffset(String videoId, long offset, DataCallback<Integer> callback);


    /**
     * Insert Download Entry in the database
     *
     * @param de       - IVideoModel object
     * @param callback
     * @return - the row ID of the newly inserted row, or -1 if an error occurred
     */
    Long addVideoData(VideoModel de, DataCallback<Long> callback);

    /**
     * Returns VideoEntry for the passed VideoId
     *
     * @param videoId
     * @param callback
     */
    VideoModel getVideoEntryByVideoId(String videoId, DataCallback<VideoModel> callback);

    /**
     * Returns {@link org.edx.mobile.model.VideoModel} which is downloaded or download is in
     * progress for given videoUrl.
     *
     * @param videoUrl
     * @param callback
     * @return
     */
    VideoModel getVideoByVideoUrl(String videoUrl, DataCallback<VideoModel> callback);


    /**
     * Marks given Video as online and sets dmid to -1 so that this video is identified as
     * NOT_DOWNLOADED. File path for this video is made empty so as to avoid access to non-existing
     * file.
     * NOTE: If there are multiple videos with same URL that are marked as Downloaded,
     * Only the reference should be removed and not the downloaded file
     *
     * @param de       - IVideoModel object
     * @param callback
     * @return - the row ID of the newly inserted row, or -1 if an error occurred
     */
    Integer deleteVideoByVideoId(VideoModel de, DataCallback<Integer> callback);

    /**
     * Marks given Video as online and sets dmid to -1 so that this video is identified as
     * NOT_DOWNLOADED. File path for this video is made empty so as to avoid access to non-existing
     * file.
     * NOTE: If there are multiple videos with same URL that are marked as Downloaded,
     * Only the reference should be removed and not the downloaded file
     *
     * @param video    - IVideoModel object
     * @param username
     * @param callback
     * @return - the row ID of the newly inserted row, or -1 if an error occurred
     */
    Integer deleteVideoByVideoId(VideoModel video, String username, DataCallback<Integer> callback);

    /**
     * Returns if a IVideoModel with the same video URL is downloaded
     * This method is used to display the progress if video is already downloaded
     *
     * @param url
     */
    Boolean isVideoFilePresentByUrl(String url, DataCallback<Boolean> callback);


    /**
     * This method updates info for Videos with the same URL and have been enqueued for downloading
     *
     * @param model
     * @param callback
     */
    Integer updateDownloadingVideoInfoByVideoId(VideoModel model, DataCallback<Integer> callback);

    /**
     * This method marks the Video as Downloading when enqueued for Download
     *
     * @param model
     * @param callback
     */
    Integer updateAsDownloadingByVideoId(VideoModel model, DataCallback<Integer> callback);

    /**
     * Returns list of All VideoEntries which are currently being downloaded
     *
     * @return
     */
    List<VideoModel> getListOfOngoingDownloads(DataCallback<List<VideoModel>> callback);

    /**
     * If the courseId is provided, returns the Videos within a course which are currently being
     * downloaded. Otherwise, returns all the videos being downloaded irrespective of course
     * they belong to.
     *
     * @param callback Callback to get list of videos being downloaded.
     * @param courseId Course's ID.
     * @return List of videos being downloaded.
     */
    List<VideoModel> getListOfOngoingDownloadsByCourseId(@Nullable String courseId,
                                                         DataCallback<List<VideoModel>> callback);


    /**
     * Returns no of Videos which have been completely
     * downloaded and marked as Downloaded in the DB
     *
     * @return - count of Videos downloaded
     */
    Integer getVideosDownloadedCount(DataCallback<Integer> callback);

    /**
     * Returns IVideoModel object if entry exists with Video status set as
     * downloaded with the given URL
     */
    VideoModel getIVideoModelByVideoUrl(String videoUrl,
                                        DataCallback<VideoModel> callback);


    /**
     * Return true if IVideoModel for associated dmId is present in db. Do not use username
     * as this function is to check if the dmid is of OpenEdXMobile or other application
     *
     * @param dmId
     * @return
     */
    Boolean isDmIdExists(long dmId, DataCallback<Boolean> callback);

    /**
     * Marks the download as complete for the given dmid.
     * NOTE - This should be done irrespective of username as if the user is
     * logged out and download is in progress, it should update download complete in the db.
     *
     * @param dmId
     * @return
     */
    Integer updateDownloadCompleteInfoByDmId(long dmId, VideoModel de,
                                             DataCallback<Integer> callback);

    /**
     * Returns list of all videos from the database.
     *
     * @return
     */
    List<VideoModel> getAllVideos(String username, DataCallback<List<VideoModel>> DataCallback);

    /**
     * Returns list of all videos from the database for a specific course.
     *
     * @param courseId Course's ID.
     * @param callback Callback to use for delivering the result.
     * @return List of all videos from the database for a specific course.
     */
    List<VideoModel> getAllVideosByCourse(@NonNull String courseId,
                                          @Nullable DataCallback<List<VideoModel>> callback);

    /**
     * Returns the list of all videos from the database for the provided video components. If
     * {@link DownloadedState downloaded state} is non-null the results are filtered accordingly, if
     * its null the results are returned as is.
     *
     * @param videoComponents Video components.
     * @param callback        Callback to use for delivering the result.
     * @param downloadedState Video download state against which the result will be filtered..
     * @return List of all videos from the database for the provided video components.
     */
    List<VideoModel> getVideosByVideoIds(@NonNull List<CourseComponent> videoComponents,
                                         @Nullable DownloadedState downloadedState,
                                         @Nullable DataCallback<List<VideoModel>> callback);

    /**
     * Removes all records of given username from the database.
     *
     * @param username
     */
    void clearDataByUser(String username);

    /**
     * This method gives the WatchedState stored in the DB for VideoID
     *
     * @param videoId
     * @param dataCallback
     */
    WatchedState getWatchedStateForVideoId(String videoId,
                                           DataCallback<WatchedState> dataCallback);

    /**
     * Returns count of videos which have given URL as their video URL.
     *
     * @param videoUrl
     * @param callback
     * @return
     */
    Integer getVideoCountByVideoUrl(String videoUrl, DataCallback<Integer> callback);

    /**
     * Returns count of videos which have given URL as their video URL.
     *
     * @param dmId
     * @param callback
     * @return
     */
    VideoModel getDownloadEntryByDmId(long dmId, DataCallback<VideoModel> callback);

    /**
     * This method gives the WatchedState stored in the DB for VideoID
     *
     * @param videoId
     * @param dataCallback
     */
    DownloadedState getDownloadedStateForVideoId(String videoId,
                                                 DataCallback<DownloadedState> dataCallback);

    /**
     * Return true if any Video is marked as Downloading for the courseId in the database for
     * logged in user
     * Used to handle reloading of Section listing
     *
     * @return boolean flag if download is in progress
     */
    Boolean isAnyVideoDownloadingInCourse(DataCallback<Boolean> callback, String courseId);

    /**
     * Return true if any Video is marked as Downloading for a section in the database for logged
     * in user
     * Used to handle reloading of subsection listing
     *
     * @return boolean flag if download is in progress
     */
    Boolean isAnyVideoDownloadingInSection(DataCallback<Boolean> callback, String courseId,
                                           String section);

    /**
     * Return true if any Video is marked as Downloading for a subsection in the database for
     * logged in user
     * Used to handle reloading of Video listing
     *
     * @return boolean flag if download is in progress
     */
    Boolean isAnyVideoDownloadingInSubSection(DataCallback<Boolean> callback, String courseId,
                                              String section, String subSection);

    /**
     * update assessment unit access record
     */
    Integer updateAccess(DataCallback<Integer> callback, String unitId, boolean visited);

    /**
     * get assessment unit access status
     */
    boolean isUnitAccessed(DataCallback<Boolean> callback, String unitId);

}
