package org.edx.mobile.module.download;

import android.content.Context;

/**
 * This class provides singleton instance of IDownloadManager.
 * 
 * @author rohan
 *
 */
public class DownloadFactory {

    private static IDownloadManager instance;
    
    /**
     * Returns singleton instance of IDownloadManager.
     * @param context
     * @return
     */
    public static IDownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new IDownloadManagerImpl(context);
        }
        
        return instance;
    }
    
}
