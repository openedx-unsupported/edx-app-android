package org.edx.mobile.services;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.task.EnqueueDownloadTask;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Singleton
public class VideoDownloadHelper {
    public interface DownloadManagerCallback {
        void onDownloadStarted(Long result);

        void onDownloadFailedToStart();

        void showProgressDialog(int numDownloads);

        void updateListUI();

        boolean showInfoMessage(String message);
    }

    protected final Logger logger = new Logger(getClass().getName());

    private DownloadSizeExceedDialog downloadFragment;

    @Inject
    IStorage storage;

    @Inject
    Config config;

    @Inject
    ISegment segment;


    public void downloadVideos(final List<HasDownloadEntry> model, final FragmentActivity activity,
                               final DownloadManagerCallback callback) {
        if (model == null || model.isEmpty()) {
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
            MediaConsentUtils.consentToMediaPlayback(activity, dialogCallback, config);

        } catch (Exception e) {
            logger.error(e);
        }

    }

    private void startDownloadVideos(List<HasDownloadEntry> model, FragmentActivity activity, DownloadManagerCallback callback) {

        long downloadSize = 0;
        ArrayList<DownloadEntry> downloadList = new ArrayList<DownloadEntry>();
        int downloadCount = 0;
        for (HasDownloadEntry v : model) {
            DownloadEntry de = v.getDownloadEntry(storage);
            if (de.downloaded == DownloadEntry.DownloadedState.DOWNLOADING
                    || de.downloaded == DownloadEntry.DownloadedState.DOWNLOADED
                    || de.isVideoForWebOnly) {
                continue;
            } else {
                downloadSize = downloadSize
                        + v.getSize();
                downloadList.add(de);
                downloadCount++;
            }
        }
        if (downloadSize > MemoryUtil
                .getAvailableExternalMemory(activity)) {
            ((BaseFragmentActivity) activity).showInfoMessage(activity.getString(R.string.file_size_exceeded));
            callback.updateListUI();
        } else {
            if (downloadSize < MemoryUtil.GB) {
                startDownload(downloadList, downloadCount, activity, callback);
            } else {
                showDownloadSizeExceedDialog(downloadList, downloadCount, activity, callback);
            }
        }
    }

    // Dialog fragment to display message to user regarding
    private void showDownloadSizeExceedDialog(final ArrayList<DownloadEntry> de,
                                              final int noOfDownloads, final FragmentActivity activity, final DownloadManagerCallback callback) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", activity.getString(R.string.download_exceed_title));
        dialogMap.put("message_1", activity.getString(R.string.download_exceed_message));
        downloadFragment = DownloadSizeExceedDialog.newInstance(dialogMap,
                new IDialogCallback() {
                    @Override
                    public void onPositiveClicked() {
                        startDownload(de, noOfDownloads, activity, callback);
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
        downloadEntries.add(downloadEntry);
        startDownload(downloadEntries, 1, activity, callback);
    }

    private void startDownload(List<DownloadEntry> downloadList,
                               int noOfDownloads, final FragmentActivity activity, final DownloadManagerCallback callback) {
        if (downloadList.isEmpty())
            return;
        try {
            if (downloadList.size() > 1) {
                segment.trackSectionBulkVideoDownload(downloadList.get(0).getEnrollmentId(),
                        downloadList.get(0).getChapterName(), noOfDownloads);
            }
        } catch (Exception e) {
            logger.error(e);
        }

        EnqueueDownloadTask downloadTask = new EnqueueDownloadTask(activity, downloadList) {
            @Override
            public void onSuccess(Long result) {
                callback.onDownloadStarted(result);
            }

            @Override
            public void onException(Exception ex) {
                callback.onDownloadFailedToStart();
            }
        };


        callback.showProgressDialog(downloadList.size());
        downloadTask.execute();
    }

}
