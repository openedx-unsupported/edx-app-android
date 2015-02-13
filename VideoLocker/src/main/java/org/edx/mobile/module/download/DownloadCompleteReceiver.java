package org.edx.mobile.module.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;

public class DownloadCompleteReceiver extends BroadcastReceiver {
    private final Logger logger = new Logger(getClass().getName());

    @Override
    public void onReceive(final Context context, Intent data) {
        try {
            if (data != null && data.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
                long id = data.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id != -1) {
                    logger.debug("Received download notification for id: " + id);

                    // check if download was SUCCESSFUL
                    IDownloadManager dm = DownloadFactory.getInstance(context);
                    NativeDownloadModel nm = dm.getDownload(id);

                    if (nm == null || nm.status != DownloadManager.STATUS_SUCCESSFUL) {
                        logger.debug("Download seems failed or cancelled for id : " + id);
                        return;
                    } else {
                        logger.debug("Download successful for id : " + id);
                    }

                    // mark download as completed
                    IStorage storage = new Storage(context);
                    storage.markDownloadAsComplete(id, new DataCallback<IVideoModel>() {
                        @Override
                        public void onResult(IVideoModel result) {
                            if(result!=null){
                                DownloadEntry download = (DownloadEntry) result;
                                
                                ISegment segIO = SegmentFactory.getInstance();
                                segIO.trackDownloadComplete(download.videoId, download.eid, 
                                        download.lmsUrl);

                                // update count of downloaded videos
                                // store user's data in his own preference file, so as to keep it unique
                                PrefManager p = new PrefManager(context, download.username);
                                long count = p.getLong(PrefManager.Key.COUNT_OF_VIDEOS_DOWNLOADED);
                                if (count < 0) {
                                    count = 0;
                                }
                                count ++;
                                p.put(PrefManager.Key.COUNT_OF_VIDEOS_DOWNLOADED, count);
                            }
                        }

                        @Override
                        public void onFail(Exception ex) {
                            logger.error(ex);
                        }
                    });
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }

    }

}
