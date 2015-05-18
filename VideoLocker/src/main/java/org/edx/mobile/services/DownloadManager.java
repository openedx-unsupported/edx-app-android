package org.edx.mobile.services;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.task.EnqueueDownloadTask;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.view.CourseDetailTabActivity;
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DownloadManager {
    public static interface DownloadManagerCallback{
        void onDownloadSuccess(Long result);
        void onDownloadFailure();
        void showProgressDialog();
        void updateListUI();
        boolean showInfoMessage(String message);
    }
    protected final Logger logger = new Logger(getClass().getName());
    private static DownloadManager manager;
    private DownloadSizeExceedDialog downloadFragment;

    public static synchronized final DownloadManager getSharedInstance(){
        if ( manager == null )
            manager = new DownloadManager();
        return manager;
    }

    public void downloadVideos(final List<VideoResponseModel> model, final FragmentActivity activity,
                               final DownloadManagerCallback callback) {
        if ( model == null || model.isEmpty() ) {
             return;
        }
        try {
            IDialogCallback dialogCallback = new IDialogCallback() {
                @Override
                public void onPositiveClicked() {
                    startDownloadVideos(model, activity, callback);
                }

                @Override
                public void onNegativeClicked() {
                    callback.showInfoMessage(activity.getString(R.string.wifi_off_message));
                }
            };
            MediaConsentUtils.consentToMediaPlayback(activity, dialogCallback);

        } catch (Exception e) {
            logger.error(e);
        }

    }

    private void startDownloadVideos(List<VideoResponseModel> model, FragmentActivity activity, DownloadManagerCallback callback) {
        Storage storage = new Storage(activity);
        long downloadSize = 0;
        ArrayList<DownloadEntry> downloadList = new ArrayList<DownloadEntry>();
        int downloadCount = 0;
        for (VideoResponseModel v : model) {
            DownloadEntry de = (DownloadEntry) storage
                .getDownloadEntryfromVideoResponseModel(v);
            if (de.downloaded == DownloadEntry.DownloadedState.DOWNLOADING
                || de.downloaded == DownloadEntry.DownloadedState.DOWNLOADED
                || de.isVideoForWebOnly ) {
                continue;
            } else {
                downloadSize = downloadSize
                    + v.getSummary().getSize();
                downloadList.add(de);
                downloadCount++;
            }
        }
        if (downloadSize > MemoryUtil
            .getAvailableExternalMemory(activity)) {
            ((CourseDetailTabActivity) activity)
                .showInfoMessage(activity.getString(R.string.file_size_exceeded));
            callback.updateListUI();
        } else {
            if (downloadSize < MemoryUtil.GB) {
                startDownload(downloadList, downloadCount,activity, callback);
            } else {
                showDownloadSizeExceedDialog(downloadList, downloadCount, activity);
            }
        }
    }

    // Dialog fragment to display message to user regarding
    private void showDownloadSizeExceedDialog(final ArrayList<DownloadEntry> de,
                                                final int noOfDownloads, FragmentActivity activity) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", activity.getString(R.string.download_exceed_title));
        dialogMap.put("message_1", activity.getString(R.string.download_exceed_message));
        downloadFragment = DownloadSizeExceedDialog.newInstance(dialogMap,
            new IDialogCallback() {
                @Override
                public void onPositiveClicked() {
                   // startDownload(de, noOfDownloads);
                }

                @Override
                public void onNegativeClicked() {
                  //  updateList();
                    downloadFragment.dismiss();
                }
            });
        downloadFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        downloadFragment.show(activity.getSupportFragmentManager(), "dialog");
        downloadFragment.setCancelable(false);
    }

    public void downloadVideo(DownloadEntry downloadEntry, final FragmentActivity activity, final DownloadManagerCallback callback) {
        List<DownloadEntry> downloadEntries = new ArrayList<>();
        downloadEntries.add( downloadEntry );
        startDownload(downloadEntries, 1, activity, callback );
    }

    private void startDownload(List<DownloadEntry> downloadList,
                              int noOfDownloads, final FragmentActivity activity, final DownloadManagerCallback callback) {
        if ( downloadList.isEmpty() )
            return;
        try{
            if ( downloadList.size() > 1 ) {
                SegmentFactory.getInstance().trackSectionBulkVideoDownload(downloadList.get(0).getEnrollmentId(),
                    downloadList.get(0).getChapterName(), noOfDownloads);
            }
        }catch(Exception e){
            logger.error(e);
        }

        EnqueueDownloadTask downloadTask = new EnqueueDownloadTask(activity) {
            @Override
            public void onFinish(Long result) {
                 callback.onDownloadSuccess(result);
            }

            @Override
            public void onException(Exception ex) {
               callback.onDownloadFailure();
            }
        };

        // it is better to show progress before executing the task
        // this ensures task will hide the progress after it is shown
        if(downloadList.size()>=3) {
            callback.showProgressDialog();
        }

        downloadTask.execute(downloadList);
    }

}
