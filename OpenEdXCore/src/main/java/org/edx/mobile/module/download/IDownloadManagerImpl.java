package org.edx.mobile.module.download;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.util.Sha1Util;

import java.io.File;

@Singleton
public class IDownloadManagerImpl implements IDownloadManager {

    private Context context;
    @Inject
    private DownloadManager dm;
    private final Logger logger = new Logger(getClass().getName());

    @Inject
    public IDownloadManagerImpl(Context context) {
        this.context = context;
    }

    @Override
    public synchronized  NativeDownloadModel getDownload(long dmid) {
        //Need to check first if the download manager service is enabled
        if(!isDownloadManagerEnabled())
            return null;

        try {
            Query query = new Query();
            query.setFilterById(dmid);

            Cursor c = dm.query(query);
            if (c.moveToFirst()) {
                long downloaded = c
                        .getLong(c
                                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long size = c.getLong(c
                        .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                String filepath = c.getString(c
                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                int status = c.getInt(c
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));

                c.close();
                
                NativeDownloadModel ndm = new NativeDownloadModel();
                ndm.dmid = dmid;
                ndm.downloaded = downloaded;
                ndm.size = size;
                ndm.filepath = filepath;
                ndm.status = status;
                
                return ndm;
            }
            c.close();
        } catch(Exception e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public synchronized long addDownload(File destFolder, String url, boolean wifiOnly) {
        long dmid = -1;

        //Need to check first if the download manager service is enabled
        if(!isDownloadManagerEnabled())
            return dmid;

        try {
            // skip if URL is not valid
            if(url == null) {
                // URL is null
                return dmid;
            }
            url = url.trim();
            if (url.length() == 0) {
                // URL is empty
                return dmid;
            }
            
            logger.debug("Starting download: " + url);
            
            Uri target = Uri.fromFile(new File(destFolder, Sha1Util.SHA1(url)));
            Request request = new Request(Uri.parse(url));
            request.setDestinationUri(target);
            request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);

            if (wifiOnly) {
                request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
            } else {
                request.setAllowedNetworkTypes(Request.NETWORK_WIFI | Request.NETWORK_MOBILE);
            }
    
            dmid = dm.enqueue(request);
        } catch(Exception ex) {
            logger.error(ex);
        }
        
        return dmid;
    }

    @Override
    public synchronized boolean removeDownload(long dmid) {
        //Need to check first if the download manager service is enabled
        if(isDownloadManagerEnabled()){
            int count = dm.remove(dmid);
            return (count == 1);
        }
        return false;
    }

    @Override
    public synchronized int getProgressForDownload(long dmid) {
        return getAverageProgressForDownloads(new long[] {dmid});
    }

    @Override
    public synchronized int getAverageProgressForDownloads(long[] dmids) {
        //Need to check first if the download manager service is enabled
        if(!isDownloadManagerEnabled())
            return 0;

        Query query = new Query();
        query.setFilterById(dmids);
        try {
            Cursor c = dm.query(query);
            if (c.moveToFirst()) {
                int count = c.getCount();
                float aggrPercent = 0;
                do {
                    long downloaded = c
                        .getLong(c
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    long size = c.getLong(c
                        .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    aggrPercent += (100f * downloaded / size);
                } while (c.moveToNext());

                c.close();

                int average = (int) (aggrPercent / count);
                return average;
            }
            c.close();
        }catch (Exception ex){
            logger.debug(ex.getMessage());
        }
        
        return 0;
    }

    @Override
    public synchronized boolean isDownloadComplete(long dmid) {
        //Need to check first if the download manager service is enabled
        if(!isDownloadManagerEnabled())
            return false;

        Query query = new Query();
        query.setFilterById(dmid);

        Cursor c = dm.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c
                    .getColumnIndex(DownloadManager.COLUMN_STATUS));
            c.close();

            return (status == DownloadManager.STATUS_SUCCESSFUL);
        }
        c.close();

        return false;
    }

    @Override
    public synchronized boolean isDownloadManagerEnabled(){
        if(context==null){
            return false;
        }

        int state = context.getPackageManager()
                .getApplicationEnabledSetting("com.android.providers.downloads");

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            //download manager is disabled
            return false;
        }
        return true;
    }
}
