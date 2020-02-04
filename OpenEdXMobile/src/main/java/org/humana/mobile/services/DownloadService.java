package org.humana.mobile.services;

import android.Manifest;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.ads.formats.NativeAdOptions;

import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.local.db.table.DownloadPeriodDesc;
import org.humana.mobile.tta.data.local.db.table.Period;

import java.io.File;

import de.greenrobot.event.EventBus;


public class DownloadService extends IntentService {
    private static final String DOWNLOAD_PATH = "download_path";
    private static final String TITLE = "title";
    private static String STATUS = "status";
    private static String PERIODID = "period_id";
    DownloadPeriodDesc periodDesc;
    DataManager mDataManager;
    DownloadManager.Query query;
    DownloadManager downloadManager;
    String downloadPath, title;
    long periodId;
    static Period period;


    public DownloadService() {
        super("DownloadService");
    }

    public static Intent getDownloadService(final @NonNull Context callingClassContext,
                                            final @NonNull String downloadPath,
                                            final @NonNull String title,
                                            final @NonNull long periodId,
                                            final @NonNull Period period1) {
        Intent in = new Intent(callingClassContext, DownloadService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(TITLE, title)
                .putExtra(PERIODID, periodId);

        period = period1;
        callingClassContext.startService(in);
        return in;

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
        title = intent.getStringExtra(TITLE);
        periodId = intent.getLongExtra(PERIODID,0);
        startDownload(downloadPath, title);
    }

    private void startDownload(String downloadPath, String title) {
        mDataManager = DataManager.getInstance(this);
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
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        Intent intent = new Intent();
        intent.setAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intent.putExtra("data", "Downloaded");
        sendBroadcast(intent);


    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                        period.setDownloadStatus("Downloaded");
                        period.setUsername(mDataManager.getLoginPrefs().getUsername());

                        mDataManager.insertPeriodDesc(period);
                        EventBus.getDefault().post(period);

                        break;
                    case DownloadManager.ACTION_NOTIFICATION_CLICKED:
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


    public static void openDownloadedFile(Uri uri, Context context){
        File file = new File(Environment.getExternalStorageDirectory()+"/"+
                Environment.DIRECTORY_DOWNLOADS,uri.getLastPathSegment());
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "*/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri data = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);;

        intent.setDataAndType(data, type);
        context.startActivity(intent);
    }



}