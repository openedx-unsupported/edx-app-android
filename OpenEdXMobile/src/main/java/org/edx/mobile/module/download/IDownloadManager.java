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
     * @param title of video
     * @return
     */
    long addDownload(File destFolder, String url, boolean wifiOnly, String title);

    /**
     * Cancel downloads and remove them from the download manager.  Each download will be stopped if
     * it was running, and it will no longer be accessible through the download manager.
     * If there is a downloaded file, partial or complete, it is deleted.
     *
     * @param dmids The IDs of the downloads to remove.
     * @return The number of downloads actually removed.
     */
    int removeDownloads(long... dmids);

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
     * Returns download progress details of provided download manager IDs.
     *
     * @param dmids Download manager IDs.
     * @return Download progress details object.
     */
    NativeDownloadModel getProgressDetailsForDownloads(long[] dmids);

    /**
     * Returns true if the Native Download Manager service is on.
     * @return
     */
    public boolean isDownloadManagerEnabled();
}
