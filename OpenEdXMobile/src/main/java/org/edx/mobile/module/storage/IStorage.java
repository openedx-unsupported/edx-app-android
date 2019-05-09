package org.edx.mobile.module.storage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.tta.analytics.AnalyticModel;
import org.edx.mobile.tta.scorm.ScormBlockModel;
import org.edx.mobile.tta.tincan.model.Resume;

import java.util.ArrayList;
import java.util.List;

public interface IStorage {

    /**
     * Adds a Video for Downloading by enqueing in Native Download Manager
     * and updating in the Database
     * @param model
     * @return row id updated in the Database (0 if not added to download)
     */
    long addDownload(VideoModel model);

    /**
     * Removes a Video from the database as well as NativeDownloadManager
     * and removing the file stored in DB
     * @param model
     * @return no of entries that were marked as deleted or removed
     */
    int removeDownload(VideoModel model);

    /**
     * Removes a list of videos from the database as well as NativeDownloadManager
     * and removes the files from storage.
     *
     * @param modelList List of video files that need to be deleted.
     * @return No of entries that were marked as deleted or removed.
     */
    int removeDownloads(List<VideoModel> modelList);

    /**
     * Removes all videos from the database as well as NativeDownloadManager.
     * This method fetches all ongoing downloads from the DB; iterates through the list
     * and then calls the {@link #removeDownload(VideoModel)} method for each video
     */
    void removeAllDownloads();

    /**
     * This method fetches all unenrolledVideos from the DB.
     * Iterates through the list and then calls the remove Download method for each video
     * Method in DB for getting all unenrolled videos is db.getAllDeactivatedVideos(callback);
     * @return
     */
    int deleteAllUnenrolledVideos();


    /**
     * Returns Download Progress percent of all the Videos
     * which are currently being downloaded
     */
    void getAverageDownloadProgress(DataCallback<Integer> callback);

    /**
     * If the courseId is provided returns the download progress percent of all the Videos
     * within a course which are currently being downloaded. Otherwise, returns the download
     * progress of the all the videos being downloaded irrespective of course they belong to.
     *
     * @param courseId Course's ID.
     * @param callback Callback to get status of videos download.
     */
    void getDownloadProgressOfCourseVideos(@Nullable String courseId,
                                           DataCallback<NativeDownloadModel> callback);

    /**
     * Returns the download progress percent of the provided Videos which are currently being
     * downloaded.
     *
     * @param videoComponents List of video components.
     * @param callback        Callback to get status of videos download.
     */
    void getDownloadProgressOfVideos(@NonNull List<CourseComponent> videoComponents,
                                     DataCallback<NativeDownloadModel> callback);

    /**
     * Returns Download Progress percent of all Videos which are currently
     * being downloadedin the chapter
     * @param enrollmentId
     * @param chapter
     * @param callback - Callback containing Integer
     */
    void getAverageDownloadProgressInChapter(String enrollmentId,
            String chapter, DataCallback<Integer> callback);

    /**
     * Returns Download Progress percent of all Videos which are currently
     * being downloaded in the section
     * @param enrollmentId
     * @param chapter
     * @param section
     * @param callback - Callback containing Integer
     */
    void getAverageDownloadProgressInSection(String enrollmentId,
            String chapter, String section, DataCallback<Integer> callback);

    /**
     * Returns DownloadEntry Model after converting it from VideoResponseModel
     * @param vrm - VideoResponseModel
     * @return
     */
    VideoModel getDownloadEntryfromVideoResponseModel(VideoResponseModel vrm);

    /**
     * Returns DownloadEntry Model after converting it from VidoeBlockModel
     * @param block - VidoeBlockModel
     * @return
     */
    VideoModel getDownloadEntryFromVideoModel(VideoBlockModel block);

    VideoModel getDownloadEntryFromScormModel(ScormBlockModel block);

    @NonNull
    Integer  getDownloadedScromCount() throws Exception;

    /**
     * Returns NativeDownload Entry for the given DMID
     * @param dmId
     * @return
     */
    NativeDownloadModel getNativeDownload(long dmId);

    /**
     * This DownloadEntry model is fetched and returned from the db
     * after reloading a few values from the DB
     * @param video
     * @return
     */
    DownloadEntry reloadDownloadEntry(DownloadEntry video);

    /**
     * Returns Download Progress percent of the DMID
     * @param dmId
     */
    void getDownloadProgressByDmid(long dmId, DataCallback<Integer> callback);

    /**
     * Marks in the db as Download completed for dmid and sets values from
     * Native manager into the db for that dmid
     * @param dmId - DownloadModule ID
     * @param callback
     */
    void markDownloadAsComplete(long dmId, DataCallback<VideoModel> callback);

    /**
     * Marks given video as WatchedState.PARTIALLY_WATCHED if it is WatchedState.WATCHED.
     * @param videoModel
     * @param watchedStateCallback
     */
    void markVideoPlaying(DownloadEntry videoModel, DataCallback<Integer> watchedStateCallback);

    void repairDownloadCompletionData();

    DownloadEntry getPostVideo(String postId);

    DownloadEntry getPostVideo(String p_id, String video_url);

    //Added by Arjun to store batch analytics
    @NonNull
    long addAnalytic(AnalyticModel model);

    @NonNull
    int removeAnalytics(String[] ids,String INQueryParams);

    ArrayList<AnalyticModel> getMxAnalytics(int batch_count, int status) throws Exception;

    ArrayList<AnalyticModel> getTincanAnalytics(int batch_count, int status) throws Exception;

    //for tincan resume handeling.
    Long addResumePayload(Resume resume);
    Integer updateResumePayload(Resume resume);
    Integer deleteResumePayload(String course_id, String unit_id);
    Resume getResumeInfo(String course_id, String unit_id);
}
