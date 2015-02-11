package org.edx.mobile.module.storage;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.api.ChapterModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SectionItemModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.DatabaseModelFactory;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.download.DownloadFactory;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.util.PropertyUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Storage implements IStorage {

    private Context context;
    private IDatabase db;
    private IDownloadManager dm;
    private UserPrefs pref;
    private final Logger logger = new Logger(getClass().getName());

    public Storage(Context context) {
        this.context = context;

        // init pref file
        this.pref = new UserPrefs(context);

        // init database manager
        UserPrefs pref = new UserPrefs(context);

        String username = pref.getProfile() == null ? null : pref.getProfile().username;
        this.db = DatabaseFactory.getInstance(context, DatabaseFactory.TYPE_DATABASE_NATIVE, username);

        // init download manager
        this.dm = DownloadFactory.getInstance(context);
    }

    public long addDownload(IVideoModel model) {
        if(model.getVideoUrl()==null||model.getVideoUrl().length()<=0){
            return -1;
        }
        IVideoModel videoByUrl = db.getVideoByVideoUrl(model.getVideoUrl(), null);

        db.addVideoData(model, null);
        //IVideoModel videoById = db.getVideoEntryByVideoId(model.getVideoId(), null);

        if (videoByUrl == null || videoByUrl.getDmId() < 0) {
            // there is no any download ever marked for this URL
            // so, add a download and map download info to given video
            long dmid = dm.addDownload(pref.getDownloadFolder(), model.getVideoUrl(),
                    pref.isDownloadOverWifiOnly());
            if(dmid==-1){
                //Download did not start for the video because of an issue in DownloadManager
                return -1;
            }
            NativeDownloadModel download = dm.getDownload(dmid);
            if(download!=null){
                // copy download info
                model.setDownloadingInfo(download);
            }
        } else {
            // download for this URL already exists, just map download info to given video
            model.setDownloadInfo(videoByUrl);
        }

        db.updateDownloadingVideoInfoByVideoId(model, new DataCallback<Integer>() {
            @Override
            public void onResult(Integer noOfRows) {
                if (noOfRows > 1) {
                    logger.warn("Should have updated only one video, " +
                            "but seems more than one videos are updated");
                }
                logger.debug("Video download info updated for " + noOfRows + " videos");
            }

            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
            }
        });

        return model.getDmId();
    }

    public int removeDownload(IVideoModel model) {
        int count = db.getVideoCountByVideoUrl(model.getVideoUrl(), null);
        if (count <= 1) {
            // if only one video exists, then mark it as DELETED
            // Also, remove its downloaded file
            dm.removeDownload(model.getDmId());
        } else {
            // there are other videos who have same video URL,
            // So, we can't delete the downloaded file
        }

        // anyways, we mark the video as DELETED
        return db.deleteVideoByVideoId(model, null);
    }

    /**
     * Deletes the physical file identified by given absolute file path.
     * Returns true if delete succeeds or if file does NOT exist, false otherwise.
     * DownloadManager actually deletes the physical file when remove method is called.
     * So, this method might not be required for removing downloads.
     * @param filepath
     * @return
     */
    private boolean deleteFile(String filepath) {
        try {
            if(filepath != null) {
                File file = new File(filepath);

                if (file.exists()) {
                    if (file.delete()) {
                        logger.debug("Deleted: " + file.getPath());
                        return true;
                    } else {
                        logger.warn("Delete failed: " + file.getPath());
                    }
                } else {
                    logger.warn("Delete failed, file does NOT exist: " + file.getPath());
                    return true;
                }
            }
        } catch(Exception e) {
            logger.error(e);
        }

        return false;
    }

    @Override
    public int deleteAllUnenrolledVideos() {
        //      Integer count = db.deletedDeactivatedVideos();
        return 0;
    }

    @Override
    public void getAverageDownloadProgressInChapter(String enrollmentId, String chapter, 
            final DataCallback<Integer> callback) {
        List<Long> dmidList = db.getDownloadingVideoDmIdsForChapter(enrollmentId, chapter, null);
        if (dmidList == null || dmidList.isEmpty()) {
            callback.onResult(Integer.valueOf(0));
            return;
        }

        try {
            long[] dmidArray = new long[dmidList.size()];
            for (int i=0; i< dmidList.size(); i++) {
                dmidArray[i] = dmidList.get(i);
            }
            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(Integer.valueOf(progress));
        } catch(Exception ex) {
            callback.sendException(ex);
            logger.error(ex);
        }
    }


    @Override
    public void getAverageDownloadProgress(final DataCallback<Integer> callback) {
        String username = pref.getProfile().username;
        IDatabase db = DatabaseFactory.getInstance(context, DatabaseFactory.TYPE_DATABASE_NATIVE, username);
        db.getListOfOngoingDownloads(new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                long[] dmids = new long[result.size()];
                for (int i=0; i< result.size(); i++) {
                    dmids[i] = result.get(i).getDmId();
                }

                IDownloadManager dm = DownloadFactory.getInstance(context);
                int averageProgress = dm.getAverageProgressForDownloads(dmids);
                callback.onResult(averageProgress);
            }

            @Override
            public void onFail(Exception ex) {
                callback.onFail(ex);
            }
        });
    }

    @Override
    public void getAverageDownloadProgressInSection(String enrollmentId,
            String chapter, String section, DataCallback<Integer> callback) {
        List<Long> dmidList = db.getDownloadingVideoDmIdsForSection(enrollmentId, chapter, section, null);
        if (dmidList == null || dmidList.isEmpty()) {
            callback.onResult(Integer.valueOf(0));
            return;
        }

        try {
            long[] dmidArray = new long[dmidList.size()];
            for (int i=0; i< dmidList.size(); i++) {
                dmidArray[i] = dmidList.get(i);
            }
            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(Integer.valueOf(progress));
        } catch(Exception ex) {
            logger.error(ex);
            callback.sendException(ex);
        }
    }

    @Override
    public IVideoModel getDownloadEntryfromVideoResponseModel(
            VideoResponseModel vrm) {
        IVideoModel video = db.getVideoEntryByVideoId(vrm.getSummary().getId(), null);
        if (video != null) {
            // we have a db entry, so return it
            return video;
        }

        return DatabaseModelFactory.getModel(vrm);
    }

    @Override
    public NativeDownloadModel getNativeDownlaod(long dmId) {
        return dm.getDownload(dmId);
    }

    @Override
    public ArrayList<EnrolledCoursesResponse> getDownloadedCoursesWithVideoCountAndSize() {
        Api api = new Api(context);
        ArrayList<EnrolledCoursesResponse> downloadedCourseList = new ArrayList<EnrolledCoursesResponse>();
        try {
            ArrayList<EnrolledCoursesResponse> enrolledCourses = api.getEnrolledCourses(true);
            if(enrolledCourses!=null && enrolledCourses.size()>0){
                for(EnrolledCoursesResponse enrolledCoursesResponse : enrolledCourses){
                    int videoCount = db.getDownloadedVideoCountByCourse(
                            enrolledCoursesResponse.getCourse().getId(),null);
                    if(videoCount>0){
                        enrolledCoursesResponse.videoCount = videoCount;
                        enrolledCoursesResponse.size = db.getDownloadedVideosSizeByCourse(
                                enrolledCoursesResponse.getCourse().getId(),null);
                        downloadedCourseList.add(enrolledCoursesResponse);
                    }
                }
                return downloadedCourseList;
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public ArrayList<SectionItemInterface> getRecentDownloadedVideosList() {
        try {
            ArrayList<SectionItemInterface> recentVideolist = new ArrayList<SectionItemInterface>();
            Api api = new Api(context);
            ArrayList<EnrolledCoursesResponse> courseList = null;

            courseList = api.getEnrolledCourses(true);

            if(courseList==null || courseList.size() ==0){
                return recentVideolist;
            }else{
                for (final EnrolledCoursesResponse course : courseList) {
                    // add all videos to the list for this course
                    List<IVideoModel> videos = db.getSortedDownloadsByDownloadedDateForCourseId(
                            course.getCourse().getId(), null);

                    // ArrayList<IVideoModel> videos = new ArrayList<IVideoModel>();
                    if (videos != null && videos.size() > 0) {
                        // add course header to the list
                        recentVideolist.add(course);
                        for (IVideoModel videoModel : videos) {
                            //TODO : Need to check how SectionItemInterface can be converted to IVideoModel
                            recentVideolist.add((SectionItemInterface)videoModel);
                        }
                    }
                }
                return recentVideolist;
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public DownloadEntry reloadDownloadEntry(DownloadEntry video) {
        try{
            DownloadEntry de = (DownloadEntry) db.getVideoEntryByVideoId(video.videoId, null);
            if (de != null) {
                video.lastPlayedOffset = de.lastPlayedOffset;
                video.watched = de.watched;
                video.downloaded = de.downloaded;
            }
            return video;
        } catch(Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    @Override
    public void getDownloadProgressByDmid(long dmId,
            DataCallback<Integer> callback) {
        if (dmId == 0) {
            callback.onResult(Integer.valueOf(0));
            return;
        }
        try {
            long[] dmidArray = new long[1];
            dmidArray[0] = dmId;

            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(Integer.valueOf(progress));
        } catch(Exception ex) {
            logger.error(ex);
            callback.sendException(ex);
        }
    }

    @Override
    public ArrayList<SectionItemInterface> getSortedOrganizedVideosByCourse(
            String courseId) {
        ArrayList<SectionItemInterface> list = new ArrayList<SectionItemInterface>();

        ArrayList<IVideoModel> downloadList = (ArrayList<IVideoModel>) db
                .getDownloadedVideoListForCourse(courseId, null);
        if(downloadList==null||downloadList.size()==0){
            return list;
        }

        Api api = new Api(context);
        try {
            Map<String, SectionEntry> courseHeirarchyMap = (LinkedHashMap<String, SectionEntry>) api
                    .getCourseHierarchy(courseId, true);

            // iterate chapters
            for (Entry<String, SectionEntry> chapterentry : courseHeirarchyMap.entrySet()) {
                boolean chapterAddedFlag=false;
                // iterate lectures
                for (Entry<String, ArrayList<VideoResponseModel>> lectureEntry : 
                    chapterentry.getValue().sections.entrySet()) {
                    boolean lectureAddedFlag=false;
                    // iterate videos 
                    for (VideoResponseModel v : lectureEntry.getValue()) {
                        for(IVideoModel de : downloadList){
                            // identify the video
                            if (de.getVideoId().equalsIgnoreCase(v.getSummary().getId())) {
                                // add this chapter to the list
                                if(!chapterAddedFlag){
                                    ChapterModel chModel = new ChapterModel();
                                    chModel.name = chapterentry.getKey();
                                    list.add(chModel);
                                    chapterAddedFlag = true;
                                }
                                if(!lectureAddedFlag){
                                    SectionItemModel lectureModel = new SectionItemModel();
                                    lectureModel.name = lectureEntry.getKey();
                                    list.add(lectureModel);
                                    lectureAddedFlag = true;
                                }

                                // add section below this chapter
                                list.add((DownloadEntry)de);
                                break;
                            }   // If condition for videoId
                        }       //for loop for downloadedvideos for CourseId
                    }           // for loop for VRM
                }               //  For loop for lectures
            }                   // For loop for Chapters
            return list;
        } catch (Exception e) {
            logger.error(e);
        }
        
        return null;
    }

    @Override
    public void markDownloadAsComplete(long dmId,
            DataCallback<IVideoModel> callback) {
        try{
            NativeDownloadModel nm = dm.getDownload(dmId);
            if (nm != null && nm.status == DownloadManager.STATUS_SUCCESSFUL) {
                long rowsAffected=0;
                {
                    //DownloadEntry e = getDownloadByDmId(dmId);
                    DownloadEntry e = (DownloadEntry) db.getDownloadEntryByDmId(dmId, null);
                    e.downloaded = DownloadEntry.DownloadedState.DOWNLOADED;
                    e.filepath = nm.filepath;
                    if(e.size<=0){
                        e.size = nm.size;
                    }
                    e.downloadedOn = System.currentTimeMillis();
                    // update file duration
                    if(e.duration==0){
                        try {
                            MediaMetadataRetriever r = new MediaMetadataRetriever();
                            FileInputStream in = new FileInputStream(new File(e.filepath));
                            r.setDataSource(in.getFD());
                            int duration = Integer
                                    .parseInt(r
                                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                            e.duration = duration/1000;
                            logger.debug("Duration updated to : " + duration);
                            in.close();
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                    }
                    rowsAffected = db.updateDownloadCompleteInfoByDmId(dmId, e, null);
                    callback.sendResult(e);
                }

            } else {
                // download not yet successful
                logger.debug("Download not yet completed");
            }
        }catch(Exception e){
            callback.sendException(e);
            logger.error(e);
        }
    }

    /**
     * Checks progress of all the videos that are being downloaded.
     * If progress of any of the downloads is 100%, then marks the video as DOWNLOADED.
     */
    public void repairDownloadCompletionData() {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.APP_INFO);
        String lastSavedVersionName = pref.getString(PrefManager.Key.APP_VERSION);
        if (lastSavedVersionName == null || !lastSavedVersionName.equals(PropertyUtil.getManifestVersionName(context))) {
            // current version not matching with the last saved version
            pref.put(PrefManager.Key.APP_VERSION, PropertyUtil.getManifestVersionName(context));

            // attempt to repair the data
            Thread maintenanceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UserPrefs userprefs = new UserPrefs(context);
                        ProfileModel profile = userprefs.getProfile();
                        if (profile == null) {
                            // user no logged in
                            return;
                        }

                        String username = profile.username;

                        IDownloadManager dm = DownloadFactory.getInstance(context);
                        IStorage storage = new Storage(context);
                        IDatabase db = DatabaseFactory.getInstance(context,
                                DatabaseFactory.TYPE_DATABASE_NATIVE, username);

                        List<Long> dmidList = db.getAllDownloadingVideosDmidList(null);
                        for (Long d : dmidList) {
                            // for each downloading video, check the percentage progress
                            boolean downloadComplete = dm.isDownloadComplete(d);
                            if (downloadComplete) {
                                // this means download is completed
                                // so the video status should be marked as DOWNLOADED, not DOWNLOADING
                                // update the video status
                                storage.markDownloadAsComplete(d, new DataCallback<IVideoModel>() {
                                    @Override
                                    public void onResult(IVideoModel result) {
                                        logger.debug("Video download marked as completed, dmid=" + result.getDmId());
                                    }

                                    @Override
                                    public void onFail(Exception ex) {
                                        logger.error(ex);
                                    }
                                });
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }
            });
            maintenanceThread.start();
        }
    }
}
