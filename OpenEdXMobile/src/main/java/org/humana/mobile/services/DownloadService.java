package org.humana.mobile.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.local.db.table.DownloadPeriodDesc;

import de.greenrobot.event.EventBus;


public class DownloadService extends IntentService {
    private static final String DOWNLOAD_PATH = "download_path";
    private static final String TITLE = "title";
    private static String STATUS = "status";
    DownloadPeriodDesc periodDesc;
    DataManager mDataManager;
    DownloadManager.Query query;
    DownloadManager downloadManager;


    public DownloadService() {
        super("DownloadService");
    }
    public static Intent getDownloadService(final @NonNull Context callingClassContext,
                                            final @NonNull String downloadPath,
                                            final @NonNull String title) {
        Intent in = new Intent(callingClassContext, DownloadService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(TITLE, title);
        callingClassContext.startService(in);
        return in;

    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
        String title = intent.getStringExtra(TITLE);
        startDownload(downloadPath, title);
    }
    private void startDownload(String downloadPath, String title) {
        Uri uri = Uri.parse(downloadPath); // Path where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        query = new DownloadManager.Query();
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE |
                DownloadManager.Request.NETWORK_WIFI);
        // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
        request.setTitle(title); // Title for notification.
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.getLastPathSegment());// Storage directory path
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        // This will start downloading
        downloadManager.enqueue(request);

        registerReceiver(onComplete, new IntentFilter("com.humana.mobile.CUSTOM_INTENT"));

    }


    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "com.humana.mobile.CUSTOM_INTENT":
                        Cursor cursor = downloadManager.query(query);
                        if (cursor.moveToFirst()) {
                            if (cursor.getCount() > 0) {
                                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    String file = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                                } else {
                                    int message = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                                    // So something here on failed.
                                }
                            }
                        }
                        break;
                    case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                        // Open downloads activity
//                        environment.getRouter().showDownloads(context);
                        break;
                }
            }


        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(onComplete);
        super.onDestroy();
    }

}