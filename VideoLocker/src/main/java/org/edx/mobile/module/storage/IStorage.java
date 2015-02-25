package org.edx.mobile.module.storage;

import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;

import java.util.ArrayList;

public interface IStorage {

    /**
     * Adds a Video for Downloading by enqueing in Native Download Manager 
     * and updating in the Database 
     * @param model
     * @return row id updated in the Database (0 if not added to download)
     */
    long addDownload(IVideoModel model);
    
    /**
     * Removes a Video from the database as well as NativeDownloadManager 
     * and removing the file stored in DB 
     * @param model
     * @return no of entries that were marked as deleted or removed
     */
    int removeDownload(IVideoModel model);
    

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
    IVideoModel getDownloadEntryfromVideoResponseModel(VideoResponseModel vrm);

    /**
     * Returns NativeDownload Entry for the given DMID
     * @param dmId
     * @return
     */
    NativeDownloadModel getNativeDownlaod(long dmId);

    /**
     * Returns List of Courses having downloaded videos.
     * The Course Model in this list contains the size of downloaded 
     * videos and no videos downloaded in the course
     * @return
     */
    ArrayList<EnrolledCoursesResponse> getDownloadedCoursesWithVideoCountAndSize();

    /**
     * Returns list of all recently downloaded videos list
     * The list contains local videos with only course header sorted based on Downloaded Date.
     * @return
     */
    ArrayList<SectionItemInterface> getRecentDownloadedVideosList();

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
     * This method will return a list of all Downloaded videos with 
     * Chapter and Section header in the Course in the order as being sent from the server
     * @param courseId
     * @return
     */
    ArrayList<SectionItemInterface> getSortedOrganizedVideosByCourse(String courseId);

    /**
     * Marks in the db as Download completed for dmid and sets values from 
     * Native manager into the db for that dmid
     * @param dmId - DownloadModule ID
     * @param callback
     */
    void markDownloadAsComplete(long dmId, DataCallback<IVideoModel> callback);

    /**
     * Marks given video as WatchedState.PARTIALLY_WATCHED if it is WatchedState.WATCHED.
     * @param videoModel
     * @param watchedStateCallback
     */
    void markVideoPlaying(DownloadEntry videoModel, DataCallback<Integer> watchedStateCallback);
}
