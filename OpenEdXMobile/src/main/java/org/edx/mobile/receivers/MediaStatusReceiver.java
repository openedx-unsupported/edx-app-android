package org.edx.mobile.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.inject.Inject;

import org.edx.mobile.event.MediaStatusChangeEvent;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.Sha1Util;

import java.io.File;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.receiver.RoboBroadcastReceiver;


/**
 * BroadcastReceiver to receive the removable storage (such as SD-card) status events.
 */

public class MediaStatusReceiver extends RoboBroadcastReceiver {
    @Inject
    private IDatabase db;
    @Inject
    private LoginPrefs loginPrefs;

    private PrefManager prefManager;

    public MediaStatusReceiver() {
    }

    @Override
    public void handleReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return;
        }
        prefManager = new PrefManager(context, PrefManager.Pref.SD_CARD);
        String username = loginPrefs.getUsername();
        String hashedUsername = (username != null) ? Sha1Util.SHA1(username) : null;

        // TODO: Is this Path a good method for Identifying the path in the DB
        // should we updated the model to have an SD Card Flag?
        final String sdCardPath = intent.getDataString().replace("file://", "");

        String action = intent.getAction();
        if (action != null) {
            boolean sdCardAvailable = false;
            switch (action) {
                case Intent.ACTION_MEDIA_REMOVED:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    sdCardAvailable = false;
                    handleSDCardUnmounted(hashedUsername, sdCardPath);
                    break;
                case Intent.ACTION_MEDIA_MOUNTED:
                    sdCardAvailable = true;
                    handleSDCardMounted(context, hashedUsername);
                    break;
            }
            EventBus.getDefault().postSticky(new MediaStatusChangeEvent(sdCardAvailable));
        }
    }

    private void handleSDCardUnmounted(String hashedUsername, final String sdCardPath) {
        db.getAllVideos(hashedUsername, new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(List<VideoModel> result) {
                for (VideoModel videoModel : result) {
                    if (videoModel.getFilePath() != null && videoModel.getFilePath().contains(sdCardPath)) {
                        updateVideoDownloadState(
                                videoModel,
                                DownloadEntry.DownloadedState.ONLINE.ordinal()
                        );
                    }
                }
            }

            @Override
            public void onFail(Exception ex) {
                Log.e(this.getClass().getSimpleName(),
                        "Unable to get to get list of Videos"
                );
            }
        });
    }

    private void handleSDCardMounted(final Context context, String hashedUsername) {
        db.getAllVideos(hashedUsername, new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(List<VideoModel> result) {
                String externalAppDir = FileUtil.getExternalAppDir(context).getAbsolutePath();
                String removableStorageAppDir = FileUtil.getRemovableStorageAppDir(context).getAbsolutePath();
                boolean downloadToSdCard = prefManager.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, false);
                for (VideoModel videoModel : result) {
                    updateVideoDownloadFilePathState(
                            videoModel,
                            downloadToSdCard,
                            externalAppDir,
                            removableStorageAppDir
                    );
                }
            }

            @Override
            public void onFail(Exception ex) {
                Log.e(this.getClass().getSimpleName(),
                        "Unable to get list of Videos"
                );
            }
        });
    }

    /**
     * Utility function to update the downloaded video file path(if multiple file are exist in phone memory / SD-Card for
     * the single video), and state in the database.
     *
     * @param videoModel             video model need to update in the database.
     * @param downloadToSdCard       user preference to identify the preferred file, and discard the other file.
     * @param externalAppDir         phone memory path where downloaded video files exist
     * @param removableStorageAppDir SD-Card storage path where downloaded video files exist.
     */
    private void updateVideoDownloadFilePathState(VideoModel videoModel, boolean downloadToSdCard,
                                                  String externalAppDir, String removableStorageAppDir) {
        String videoPath = videoModel.getFilePath();
        File file = new File(videoPath);
        if (file.exists()) {

            File duplicateFile = getDuplicateFile(
                    videoPath,
                    externalAppDir,
                    removableStorageAppDir);

            if (duplicateFile != null && duplicateFile.exists()) {
                if (duplicateFile.getAbsolutePath().contains(removableStorageAppDir) && downloadToSdCard) {
                    videoModel = updateVideoModelWithPreferredFilePath(
                            videoModel,
                            duplicateFile,
                            file);
                } else if (file.getAbsolutePath().contains(externalAppDir) && !downloadToSdCard) {
                    videoModel = updateVideoModelWithPreferredFilePath(
                            videoModel,
                            file,
                            duplicateFile);
                } else {
                    FileUtil.deleteRecursive(duplicateFile);
                }
            }

            updateVideoDownloadState(
                    videoModel,
                    DownloadEntry.DownloadedState.DOWNLOADED.ordinal()
            );
        }
    }

    /**
     * Utility function to update the video model based on the user preferred file(if exist)
     * and delete on old file.
     *
     * @param videoModel    Video model need to update.
     * @param preferredFile user preferred file
     * @param duplicateFile duplicate file need to delete after update the preferred file path
     * @return return update video model if downloaded file exist.
     */
    private VideoModel updateVideoModelWithPreferredFilePath(VideoModel videoModel, File preferredFile, File duplicateFile) {
        DownloadEntry videoDownloadEntry = new DownloadEntry();
        videoDownloadEntry.setDownloadInfo(videoModel);
        videoDownloadEntry.filepath = preferredFile.getAbsolutePath();
        videoDownloadEntry.videoId = videoModel.getVideoId();
        videoModel = videoDownloadEntry;
        FileUtil.deleteRecursive(duplicateFile);
        return videoModel;
    }

    /**
     * Utility function to find the duplicate file path in Phone memory / SD-Card from the given
     * path if there are two files in for the same video in Phone memory
     * and SD-Card.
     *
     * @param videoPath              video path from the database.
     * @param externalAppDir         Phone memory storage path.
     * @param removableStorageAppDir SD-Card storage path.
     * @return return duplicate file.
     */
    private File getDuplicateFile(String videoPath, String externalAppDir, String removableStorageAppDir) {
        File downloadedFile = null;
        if (videoPath.contains(externalAppDir)) {
            downloadedFile = new File(videoPath.replace(externalAppDir, removableStorageAppDir));
        } else if (videoPath.contains(removableStorageAppDir)) {
            downloadedFile = new File(videoPath.replace(removableStorageAppDir, externalAppDir));
        }
        return downloadedFile;
    }

    /**
     * Function to update the the status and the video downloaded file info in database.
     *
     * @param videoModel    Video Model need to update in database.
     * @param downloadState new download status base on file existence.
     */
    public void updateVideoDownloadState(VideoModel videoModel,
                                         int downloadState) {
        NativeDownloadModel dm = new NativeDownloadModel();
        dm.dmid = videoModel.getDmId();
        dm.filepath = videoModel.getFilePath();
        dm.size = videoModel.getSize();
        dm.downloaded = downloadState;
        videoModel.setDownloadInfo(dm);
        db.updateDownloadingVideoInfoByVideoId(videoModel, null);
    }
}
