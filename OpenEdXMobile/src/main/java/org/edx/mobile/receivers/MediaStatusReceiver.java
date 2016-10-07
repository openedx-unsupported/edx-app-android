package org.edx.mobile.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Switch;

import com.google.inject.Inject;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.Sha1Util;


import java.io.File;
import java.util.List;


/**
 * Class containing the Broadcast receiver to detect the status of Removable media
 */
public class MediaStatusReceiver extends BroadcastReceiver{

    @Inject
    private IDatabase db;
    @Inject
    private LoginPrefs loginPrefs;

    public MediaStatusReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TEST RECEIVER", "Media UPDATE!!!");
        db = DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE, context);

        loginPrefs = MainApplication.getEnvironment(context).getLoginPrefs();
        String username = loginPrefs.getUsername();
        String username_sha = (username != null) ? Sha1Util.SHA1(username) : null;
        final String action_path = intent.getDataString().replace("file://", "");

        switch (intent.getAction()) {
            case Intent.ACTION_MEDIA_REMOVED:
            case Intent.ACTION_MEDIA_UNMOUNTED:
                final PrefManager prefManager =
                        new PrefManager(context, PrefManager.Pref.SD_CARD);
                prefManager.put(PrefManager.Key.DOWNLOAD_TO_SDCARD, false);
                Log.i("TEST RECEIVER", "CARD REMOVED");
                db.getAllVideos(username_sha, new DataCallback<List<VideoModel>>() {
                    @Override
                    public void onResult(List<VideoModel> result) {
                        for (VideoModel videoModel : result) {
                            String current_path = videoModel.getFilePath();

                            if (current_path.contains(action_path) &&
                                    Environment.isExternalStorageRemovable(new File(current_path))) {
                                NativeDownloadModel dm = new NativeDownloadModel();
                                dm.dmid = videoModel.getDmId();
                                dm.filepath = videoModel.getFilePath();
                                dm.size = videoModel.getSize();
                                dm.downloaded = DownloadEntry.DownloadedState.ONLINE.ordinal();
                                videoModel.setDownloadInfo(dm);
                                db.updateDownloadingVideoInfoByVideoId(videoModel, new DataCallback<Integer>() {
                                    @Override
                                    public void onResult(Integer result) {
                                        Log.i("TEST RECEIVER", "Video updated as online!!!");

                                    }

                                    @Override
                                    public void onFail(Exception ex) {

                                    }
                                });
                            }
                        }
                        Log.i("TEST RECEIVER", "Data Result for All videos!!!");
                    }

                    @Override
                    public void onFail(Exception ex) {
                        Log.i("TEST RECEIVER", "FAIL !!! Data Result for All videos!!!");

                    }
                });
                break;
        case Intent.ACTION_MEDIA_MOUNTED:
            db.getAllVideos(username_sha, new DataCallback<List<VideoModel>>() {
                @Override
                public void onResult(List<VideoModel> result) {
                    for (VideoModel videoModel : result) {
                        String current_path = videoModel.getFilePath();

                        if (current_path.contains(action_path) &&
                                Environment.isExternalStorageRemovable(new File(current_path))) {

                            NativeDownloadModel dm = new NativeDownloadModel();
                            dm.dmid = videoModel.getDmId();
                            dm.filepath = videoModel.getFilePath();
                            dm.size = videoModel.getSize();
                            dm.downloaded = DownloadEntry.DownloadedState.DOWNLOADED.ordinal();
                            videoModel.setDownloadInfo(dm);
                            db.updateDownloadingVideoInfoByVideoId(videoModel, new DataCallback<Integer>() {
                                @Override
                                public void onResult(Integer result) {
                                    Log.i("TEST RECEIVER", "Video updated as Downloaded!!!!");
                                }

                                @Override
                                public void onFail(Exception ex) {

                                }
                            });
                        }
                    }
                    Log.i("TEST RECEIVER", "Data Result for All videos!!!");
                }

                @Override
                public void onFail(Exception ex) {
                    Log.i("TEST RECEIVER", "FAIL !!! Data Result for All videos!!!");

                }
            });
            Log.i("TEST RECEIVER", "CARD MOUNTED");
            break;
        }
    }

//    private class AllVideosCallback implements DataCallback<List>
}
