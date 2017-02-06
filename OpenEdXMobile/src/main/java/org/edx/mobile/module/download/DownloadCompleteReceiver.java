package org.edx.mobile.module.download;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.VideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.db.DataCallback;

import roboguice.receiver.RoboBroadcastReceiver;

public class DownloadCompleteReceiver extends RoboBroadcastReceiver {
    private final Logger logger = new Logger(getClass().getName());

    @Inject
    private IEdxEnvironment environment;

    @Override
    protected void handleReceive(final Context context, final Intent data) {
        if (data != null && data.getAction() != null) {
            switch (data.getAction()) {
                case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                    handleDownloadCompleteIntent(data);
                    break;
                case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                    // Open downloads activity
                    environment.getRouter().showDownloads(context);
                    break;
            }
        }
    }

    private void handleDownloadCompleteIntent(final Intent data) {
        if (data.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
            long id = data.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id != -1) {
                logger.debug("Received download notification for id: " + id);

                // check if download was SUCCESSFUL
                NativeDownloadModel nm = environment.getDownloadManager().getDownload(id);

                if (nm == null || nm.status != DownloadManager.STATUS_SUCCESSFUL) {
                    logger.debug("Download seems failed or cancelled for id : " + id);
                    return;
                } else {
                    logger.debug("Download successful for id : " + id);
                }

                // mark download as completed
                environment.getStorage().markDownloadAsComplete(id, new DataCallback<VideoModel>() {
                    @Override
                    public void onResult(VideoModel result) {
                        if (result != null) {
                            DownloadEntry download = (DownloadEntry) result;

                            AnalyticsRegistry analyticsRegistry = environment.getAnalyticsRegistry();
                            analyticsRegistry.trackDownloadComplete(download.videoId, download.eid,
                                    download.lmsUrl);
                        }
                    }

                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                    }
                });
            }
        }
    }

}
