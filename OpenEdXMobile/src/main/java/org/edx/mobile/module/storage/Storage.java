package org.edx.mobile.module.storage;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.AudioModel;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.AudioBlockModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.DatabaseModelFactory;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.Sha1Util;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.http.util.CallUtil.executeStrict;

@Singleton
public class Storage implements IStorage {

    @Inject
    private Context context;
    @Inject
    private IDatabase db;
    @Inject
    private IDownloadManager dm;
    @Inject
    private UserPrefs pref;
    @Inject
    private Config config;
    @Inject
    private LoginPrefs loginPrefs;
    @Inject
    private CourseAPI api;

    private final Logger logger = new Logger(getClass().getName());


    public long addDownload(DownloadEntry model) {
        if(model.getDownloadUrl()==null||model.getDownloadUrl().length()<=0){
            return -1;
        }

        DownloadEntry videoByUrl = db.getDownloadEntryByMediaUrl(model.getDownloadUrl(), null);

        db.addMediaData(model, null);

        if(model.isVideoForWebOnly())
            return -1;  //we may need to return different error code.
                        //but for now we show same generic error message
        //IVideoModel videoById = db.getVideoEntryByVideoId(model.getBlockId(), null);

        if (videoByUrl == null || videoByUrl.getDmId() < 0) {
            boolean downloadPreference = pref.isDownloadOverWifiOnly();
            if(NetworkUtil.isOnZeroRatedNetwork(context, config)){
                //If the device has zero rated network, then allow downloading
                //on mobile network even if user has "Only on wifi" settings as ON
                downloadPreference = false;
            }

            // Fail the download if download directory isn't available
            final File downloadDirectory = pref.getDownloadDirectory();
            if (downloadDirectory == null) return -1;

            // there is no any download ever marked for this URL
            // so, add a download and map download info to given video
            long dmid = dm.addDownload(downloadDirectory, model.getDownloadUrl(),
                    downloadPreference, model.getTitle());
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

    public int removeDownload(VideoModel model) {
        int count = db.getVideoCountByVideoUrl(model.getVideoUrl(), null);
        if (count <= 1) {
            // if only one video exists, then mark it as DELETED
            // Also, remove its downloaded file
            dm.removeDownloads(model.getDmId());
            deleteFile(model.getFilePath());
        }

        // anyways, we mark the video as DELETED
        int videosDeleted = db.deleteVideoByVideoId(model, null);
        EventBus.getDefault().post(new DownloadedVideoDeletedEvent());
        return videosDeleted;
    }

    public void removeAllDownloads() {
        final String username = loginPrefs.getUsername();
        final String sha1Username;
        if (TextUtils.isEmpty(username)) {
            return;
        } else {
            sha1Username = Sha1Util.SHA1(username);
        }

        // Get all on going downloads
        db.getListOfOngoingDownloads(new DataCallback<List<DownloadEntry>>(false) {
            @Override
            public void onResult(List<DownloadEntry> result) {
                // Remove all downloads from db
                long [] videoIds = new long[result.size()];
                VideoModel model;
                for (int i=0; i<result.size(); i++) {
                    model = result.get(i);
                    db.deleteVideoByVideoId(model, null, sha1Username);
                    videoIds[i] = model.getDmId();
                }
                // Remove all downloads from NativeDownloadManager
                dm.removeDownloads(videoIds);
                EventBus.getDefault().post(new DownloadedVideoDeletedEvent());
            }

            @Override
            public void onFail(Exception ex) {
            }
        });
    }

    /**
     * Deletes the physical file identified by given absolute file path.
     * Returns true if delete succeeds or if file does NOT exist, false otherwise.
     * DownloadManager actually deletes the physical file when remove method is called.
     * So, this method might not be required for removing downloads.
     * @param filepath The file to delete
     * @return true if delete succeeds or if file does NOT exist, false otherwise.
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
            callback.onResult(0);
            return;
        }

        try {
            long[] dmidArray = new long[dmidList.size()];
            for (int i=0; i< dmidList.size(); i++) {
                dmidArray[i] = dmidList.get(i);
            }
            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(progress);
        } catch(Exception ex) {
            callback.sendException(ex);
            logger.error(ex);
        }
    }


    @Override
    public void getAverageDownloadProgress(final DataCallback<Integer> callback) {
        IDatabase db = DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE );
        db.getListOfOngoingDownloads(new DataCallback<List<DownloadEntry>>() {

            @Override
            public void onResult(List<DownloadEntry> result) {
                long[] dmids = new long[result.size()];
                for (int i=0; i< result.size(); i++) {
                    dmids[i] = result.get(i).getDmId();
                    logger.debug("xxxxxxxx =" +dmids[i]);
                }

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
        long[] dmidArray = db.getDownloadingVideoDmIdsForSection(enrollmentId, chapter, section, null);
        if (dmidArray == null || dmidArray.length == 0) {
            callback.onResult(0);
            return;
        }

        try {
            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(progress);
        } catch(Exception ex) {
            logger.error(ex);
            callback.sendException(ex);
        }
    }

    @Override
    public VideoModel getDownloadEntryfromVideoResponseModel(
            VideoResponseModel vrm) {
        VideoModel video = db.getDownloadEntryByMediaId(vrm.getSummary().getId(), null);
        if (video != null) {
            // we have a db entry, so return it
            return video;
        }

        return DatabaseModelFactory.getModel(vrm);
    }

    @Override
    public VideoModel getDownloadEntryFromVideoModel(VideoBlockModel block){
        VideoModel video = db.getDownloadEntryByMediaId(block.getId(), null);
        if (video != null) {
            return video;
        }

        return DatabaseModelFactory.getModel(block.getData(), block);
    }

    @Override
    public AudioModel getDownloadEntryFromAudioModel(AudioBlockModel block){
        AudioModel audio = db.getAudioEntryByAudioId(block.getId(), null);
        if (audio != null) {
            return audio;
        }

        return DatabaseModelFactory.getModel(block.getData(), block);
    }

    @Override
    public NativeDownloadModel getNativeDownload(long dmId) {
        return dm.getDownload(dmId);
    }

    @Override
    public DownloadEntry reloadDownloadEntry(DownloadEntry video) {
        try{
            DownloadEntry de = (DownloadEntry) db.getDownloadEntryByMediaId(video.blockId, null);
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
            callback.onResult(0);
            return;
        }
        try {
            long[] dmidArray = new long[1];
            dmidArray[0] = dmId;

            int progress = dm.getAverageProgressForDownloads(dmidArray);
            callback.sendResult(progress);
        } catch(Exception ex) {
            logger.error(ex);
            callback.sendException(ex);
        }
    }

    @Override
    public void markDownloadAsComplete(long dmId,
            DataCallback<VideoModel> callback) {
        try{
            NativeDownloadModel nm = dm.getDownload(dmId);
            if (nm != null && nm.status == DownloadManager.STATUS_SUCCESSFUL) {
                {
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
                    db.updateDownloadCompleteInfoByDmId(dmId, e, null);
                    callback.sendResult(e);
                    EventBus.getDefault().post(new DownloadCompletedEvent());
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
     * NOTE - precondition - used only for app upgrade
     */
    public void repairDownloadCompletionData() {

            // attempt to repair the data
            Thread maintenanceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProfileModel profile = loginPrefs.getCurrentUserProfile();
                        if (profile == null) {
                            // user no logged in
                            return;
                        }

                        List<Long> dmidList = db.getAllDownloadingVideosDmidList(null);
                        for (Long d : dmidList) {
                            // for each downloading video, check the percentage progress
                            boolean downloadComplete = dm.isDownloadComplete(d);
                            if (downloadComplete) {
                                // this means download is completed
                                // so the video status should be marked as DOWNLOADED, not DOWNLOADING
                                // update the video status
                                markDownloadAsComplete(d, new DataCallback<VideoModel>() {
                                    @Override
                                    public void onResult(VideoModel result) {
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

    @Override
    public void markMediaPlaying(DownloadEntry videoModel, final DataCallback<Integer> watchedStateCallback) {
        try {
            final DownloadEntry v = videoModel;
            if (v != null) {
                if (v.watched == DownloadEntry.WatchedState.UNWATCHED) {
                    videoModel.watched = DownloadEntry.WatchedState.PARTIALLY_WATCHED;

                    // video entry might not exist in the database, add it
                    db.addMediaData(videoModel, new DataCallback<Long>() {
                        @Override
                        public void onResult(Long result) {
                            try {
                                // mark this as partially watches, as playing has started
                                db.updatePlayableMediaWatchedState(v.getBlockId(), DownloadEntry.WatchedState.PARTIALLY_WATCHED,
                                        watchedStateCallback);
                            } catch (Exception ex) {
                                logger.error(ex);
                            }
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
}
