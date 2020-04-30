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
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.local.db.table.DownloadPeriodDesc;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.local.db.table.CurricullamChaptersModel;

import java.io.File;


public class DownloadService extends IntentService {
    private static final String DOWNLOAD_PATH = "download_path";
    private static final String TITLE = "title";
    private static String STATUS = "status";
    private static String PERIODID = "period_id";
    private static String FILTER_TEXT = "org.humana.mobile.downaload";
    DownloadPeriodDesc periodDesc;
    DataManager mDataManager;
    IEdxEnvironment edxEnvironment;
    DownloadManager.Query query;
    DownloadManager downloadManager;
    String downloadPath, title;
    long periodId;
    static Period period;
    static CurricullamChaptersModel chapter;
    long downloadId;
    static FragmentActivity activitys;
    public static boolean isDownload = false;
    public static boolean isCurriculamm = false;



    public DownloadService() {
        super("DownloadService");
    }

    public static Intent getDownloadService(final @NonNull FragmentActivity callingClassContext,
                                            final @NonNull String downloadPath,
                                            final @NonNull String title,
                                            final @NonNull long periodId,
                                            final @NonNull Period period1) {
        Intent in = new Intent(callingClassContext, DownloadService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(TITLE, title)
                .putExtra(PERIODID, periodId);

        period = period1;
        activitys = callingClassContext;
        callingClassContext.startService(in);
        return in;

    }
    public static Intent getDownloadService(final @NonNull FragmentActivity callingClassContext,
                                            final @NonNull String downloadPath,
                                            final @NonNull String title,
                                            final @NonNull CurricullamChaptersModel chaptersModel,
                                            final @NonNull Boolean isCurriculam) {
        Intent in = new Intent(callingClassContext, DownloadService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(TITLE, title);

        chapter = chaptersModel;
        isCurriculamm = isCurriculam;
        activitys = callingClassContext;
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
        edxEnvironment = mDataManager.getEdxEnvironment();
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
        if (isCurriculamm){
            downloadId = downloadManager.enqueue(request);
            registerReceiver(onComplete, new IntentFilter("org.humana.mobile.download.curricullam"));

            Intent intent = new Intent();
            intent.setAction("org.humana.mobile.download.curricullam");
            intent.putExtra("data", "Downloaded");
            intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
            sendBroadcast(intent);
        }else {
            downloadId = downloadManager.enqueue(request);
            registerReceiver(onComplete, new IntentFilter(FILTER_TEXT));

            Intent intent = new Intent();
            intent.setAction(FILTER_TEXT);
            intent.putExtra("data", "Downloaded");
            intent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
            sendBroadcast(intent);
        }
        isDownload = true;

        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            checkStatus(cursor);
        }
    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (intent != null && intent.getAction() != null) {
//                if (intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
//                    final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//                    NativeDownloadModel nm = edxEnvironment.getDownloadManager().getDownload(id);
//                    if (nm == null || nm.status != DownloadManager.STATUS_SUCCESSFUL) {
//                        Log.d("Downloadfail:" , String.valueOf(id));
//                        return;
//                    } else {
//                        Log.d("downloadSuccess:" , String.valueOf(id));
//                        period.setDownloadStatus("Downloaded");
//                        period.setUsername(mDataManager.getLoginPrefs().getUsername());
//
//                        mDataManager.insertPeriodDesc(period);
//                        EventBus.getDefault().post(period);
//                    }
                    switch (intent.getAction()) {
                        case "org.humana.mobile.downaload":
                            period.setDownloadStatus("Downloaded");
                            period.setUsername(mDataManager.getLoginPrefs().getUsername());
                            period.setAbout_url(downloadPath);

                            mDataManager.insertPeriodDesc(period);
//                            EventBus.getDefault().post(period);

                            break;
                        case "org.humana.mobile.download.curricullam":
                            chapter.setDownloadStatus("Downloaded");
                            chapter.setTitle(title);
                            chapter.setUrl(downloadPath);
                            mDataManager.insertCurriculam(chapter);
                            break;
                        case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                            break;
                    }
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        checkStatus(cursor);
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

    public void checkStatus(Cursor cursor) {

        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        //get the download filename
        int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
        String filename = cursor.getString(filenameIndex);

        String statusText = "";
        String reasonText = "";

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                Log.d("STATUS_FAILED", statusText);
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                Log.d("STATUS_PAUSED", statusText);
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                Log.d("STATUS_PENDING", statusText);
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                Log.d("STATUS_RUNNING", statusText);

                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                Log.d("STATUS_SUCCESSFUL", statusText);

                reasonText = "Filename:\n" + filename;
                break;
        }

    }



}