package org.edx.mobile.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.inject.Inject;

import org.edx.mobile.event.MediaStatusEvent;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.Sha1Util;

import de.greenrobot.event.EventBus;
import roboguice.receiver.RoboBroadcastReceiver;


import java.io.File;
import java.util.List;


/**
 * BroadcastReceiver to receive the removable storage (such as SD-card) status events.
 */
public class MediaStatusReceiver extends RoboBroadcastReceiver {
    @Inject
    private IDatabase db;
    @Inject
    private LoginPrefs loginPrefs;

    public MediaStatusReceiver() {
    }

    @Override
    public void handleReceive(Context context, Intent intent) {
        String username = loginPrefs.getUsername();
        String hashedUsername = (username != null) ? Sha1Util.SHA1(username) : null;

        // TODO: Is this Path a good method for Identifying the path in the DB
        // should we updated the model to have an SD Card Flag?
        final String sdCardPath = intent.getDataString().replace("file://", "");

        String action = intent.getAction();
        if(action != null) {
            boolean sdCardAvailable = false;
            switch (action) {
                case Intent.ACTION_MEDIA_REMOVED:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    sdCardAvailable = false;
                    handleSDCardUnmounted(hashedUsername, sdCardPath);
                    break;
                case Intent.ACTION_MEDIA_MOUNTED:
                    sdCardAvailable = true;
                    handleSDCardMounted(hashedUsername, sdCardPath);
                    break;
            }
            EventBus.getDefault().postSticky(new MediaStatusEvent(sdCardAvailable));
        }
    }

    private void handleSDCardUnmounted(String hashedUsername, final String sdCardPath){
        db.getAllVideos(hashedUsername, new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(List<VideoModel> result) {
                for (VideoModel videoModel : result) {
                    String videoPath = videoModel.getFilePath();
                    updateVideoDownloadState(
                            videoModel,
                            DownloadEntry.DownloadedState.ONLINE.ordinal(),
                            sdCardPath,
                            videoPath
                    );
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

    private void handleSDCardMounted(String hashedUsername, final String sdCardPath){
        db.getAllVideos(hashedUsername, new DataCallback<List<VideoModel>>() {
            @Override
            public void onResult(List<VideoModel> result) {
                for (VideoModel videoModel : result) {
                    String videoPath = videoModel.getFilePath();
                    File file = new File(videoPath);
                    if (file.exists()) {
                        updateVideoDownloadState(
                                videoModel,
                                DownloadEntry.DownloadedState.DOWNLOADED.ordinal(),
                                sdCardPath,
                                videoPath
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

    public void updateVideoDownloadState(VideoModel videoModel,
                                         int downloadState,
                                         String sdCardPath,
                                         String videoPath) {
        if (videoPath.contains(sdCardPath)) {
            File testFileExists = new File(videoPath);
            if (testFileExists.exists()) {
                NativeDownloadModel dm = new NativeDownloadModel();
                dm.dmid = videoModel.getDmId();
                dm.filepath = videoModel.getFilePath();
                dm.size = videoModel.getSize();
                dm.downloaded = downloadState;
                videoModel.setDownloadInfo(dm);
                db.updateDownloadingVideoInfoByVideoId(videoModel, null);
            }
        }
    }
}
