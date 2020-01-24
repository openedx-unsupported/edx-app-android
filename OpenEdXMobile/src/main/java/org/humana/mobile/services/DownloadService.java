package org.humana.mobile.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DownloadService extends IntentService {
    private static final String DOWNLOAD_PATH = "download_path";
    private static final String TITLE = "title";
    private static final String STATUS = "status";

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
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE |
                DownloadManager.Request.NETWORK_WIFI);
        // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
        request.setTitle(title); // Title for notification.
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + title, uri.getLastPathSegment());// Storage directory path
        ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);// This will start downloading
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            intent.putExtra(STATUS, "downloadComplete");
        }
    };
}