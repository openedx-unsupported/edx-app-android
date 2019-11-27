package org.edx.mobile.services;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.storage.BulkVideosDownloadCancelledEvent;
import org.edx.mobile.module.storage.BulkVideosDownloadStartedEvent;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.task.EnqueueDownloadTask;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.util.DownloadUtil.isDownloadSizeWithinLimit;

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

    protected static final Logger logger = new Logger(VideoDownloadHelper.class.getName());

    private DownloadSizeExceedDialog downloadFragment;

    @Inject
    private IStorage storage;

    @Inject
    private AnalyticsRegistry analyticsRegistry;


    public void downloadVideos(final List<? extends HasDownloadEntry> model, final FragmentActivity activity,
                               final DownloadManagerCallback callback) {
        if (model == null || model.isEmpty()) {
            return;
        }
        IDialogCallback dialogCallback = new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                startDownloadVideos(model, activity, callback);
            }

            @Override
            public void onNegativeClicked() {
                callback.showInfoMessage(activity.getString(R.string.wifi_off_message));
                EventBus.getDefault().post(new BulkVideosDownloadCancelledEvent());
            }
        };
        MediaConsentUtils.requestStreamMedia(activity, dialogCallback);
    }

    private void startDownloadVideos(List<? extends HasDownloadEntry> model, FragmentActivity activity, DownloadManagerCallback callback) {
        long downloadSize = 0;
        ArrayList<DownloadEntry> downloadList = new ArrayList<>();
        int downloadCount = 0;
        for (HasDownloadEntry v : model) {
            DownloadEntry de = v.getDownloadEntry(storage);
            if (!TextUtils.isEmpty(v.getDownloadUrl())) {
                // Prefer download url to download
                de.url = v.getDownloadUrl();
            }
            if (null == de
                    || de.downloaded == DownloadEntry.DownloadedState.DOWNLOADING
                    || de.downloaded == DownloadEntry.DownloadedState.DOWNLOADED
                    || de.isVideoForWebOnly) {
                continue;
            } else {
                downloadSize = downloadSize
                        + de.getSize();
                downloadList.add(de);
                downloadCount++;
            }
        }
        if (downloadSize > MemoryUtil
                .getAvailableExternalMemory(activity)) {
            ((BaseFragmentActivity) activity).showInfoMessage(activity.getString(R.string.file_size_exceeded));
            callback.updateListUI();
            EventBus.getDefault().post(new BulkVideosDownloadCancelledEvent());
        } else {
            if (isDownloadSizeWithinLimit(downloadSize, MemoryUtil.GB) && !downloadList.isEmpty()) {
                startDownload(downloadList, activity, callback);

                final DownloadEntry downloadEntry = downloadList.get(0);
                analyticsRegistry.trackSubSectionBulkVideoDownload(downloadEntry.getSectionName(),
                        downloadEntry.getChapterName(), downloadEntry.getEnrollmentId(),
                        downloadCount);
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
                        if (!de.isEmpty()) {
                            startDownload(de, activity, callback);

                            final DownloadEntry downloadEntry = de.get(0);
                            analyticsRegistry.trackSubSectionBulkVideoDownload(downloadEntry.getSectionName(),
                                    downloadEntry.getChapterName(), downloadEntry.getEnrollmentId(),
                                    noOfDownloads);
                            EventBus.getDefault().post(new BulkVideosDownloadStartedEvent());
                        }
                    }

                    @Override
                    public void onNegativeClicked() {
                        //  updateList();
                        downloadFragment.dismiss();
                        EventBus.getDefault().post(new BulkVideosDownloadCancelledEvent());
                    }
                });
        downloadFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        downloadFragment.show(activity.getSupportFragmentManager(), "dialog");
        downloadFragment.setCancelable(false);
    }

    public void downloadVideo(DownloadEntry downloadEntry, final FragmentActivity activity, final DownloadManagerCallback callback) {
        if (downloadEntry != null) {
            List<DownloadEntry> downloadEntries = new ArrayList<>();
            downloadEntries.add(downloadEntry);
            startDownload(downloadEntries, activity, callback);
            analyticsRegistry.trackSingleVideoDownload(downloadEntry.getVideoId(),
                    downloadEntry.getEnrollmentId(), downloadEntry.getVideoUrl());
        }
    }

    private void startDownload(List<DownloadEntry> downloadList,
                               final FragmentActivity activity,
                               final DownloadManagerCallback callback) {
        if (downloadList.isEmpty()) return;

        EnqueueDownloadTask downloadTask = new EnqueueDownloadTask(activity, downloadList) {
            @Override
            public void onSuccess(Long result) {
                callback.onDownloadStarted(result);
                logger.debug("EnqueueDownloadTask: STARTED = " + result);
            }

            @Override
            public void onException(Exception ex) {
                super.onException(ex);
                callback.onDownloadFailedToStart();
                logger.warn("EnqueueDownloadTask: FAILED \n" + ex.toString());
            }
        };

        callback.showProgressDialog(downloadList.size());
        downloadTask.setTaskProcessCallback(null);
        downloadTask.execute();
    }
}
