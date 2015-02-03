package org.edx.mobile.module.download;

import org.edx.mobile.model.download.NativeDownloadModel;

import java.io.File;

public interface IDownloadManager {

    /**
     * Returns {@link org.edx.mobile.model.download.NativeDownloadModel} for the given dmid.
     * Returns null if no download exists for the given dmid.
     * @param dmid
     * @return
     */
    NativeDownloadModel getDownload(long dmid);
    
    /**
     * Add a new download for the given URL and returns dmid.
     * Returns -1 if fails to start download due to invalid URL or by any other reason.
     * This method does not check if the download for the same URL already exists.
     * @param destFolder
     * @param url
     * @param wifiOnly
     * @return
     */
    long addDownload(File destFolder, String url, boolean wifiOnly);
    
    /**
     * Removes download by given dmid. Returns true if download is cancelled 
     * and removed successfully, false otherwise.
     * Physical file, if it was partially or completely downloaded, is also removed. 
     * @param dmid
     * @return
     */
    boolean removeDownload(long dmid);
    
    /**
     * Returns progress of download for given dmid.
     * Returns 0 (zero) if the download has not yet been started.
     * For a completed download, returns 100.
     * @param dmid
     * @return
     */
    int getProgressForDownload(long dmid);

    /**
     * Returns true if download for given dmid is completed, false otherwise.
     * @param dmid
     * @return
     */
    boolean isDownloadComplete(long dmid);
    
    /**
     * Returns average progress of the downloads identified by given ids.
     * Returns 0 (zero) if none of these downloads are yet started.
     * Returns 100 if all the downloads are completed.
     * @param dmids
     * @return
     */
    int getAverageProgressForDownloads(long[] dmids);

    /**
     * Returns true if the Native Download Manager service is on.
     * @return
     */
    public boolean isDownloadManagerEnabled();
}
