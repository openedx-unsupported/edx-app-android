package org.edx.mobile.model;

/*
 * TODO: models to be refactored in GA+1
 */
interface IDownload {

    void setDownloaded(long downloadedSizeInBytes);

    void setTotalSize(long sizeInBytes);

    void setFilePath(String filepath);

    void setDownloadStatus(int status);

    boolean isDownloaded();

    boolean isDownloadingInProgress();

    String getFilePath();

    String getTotalSizeInMB();

    String getDownloadedSizeInMB();
}
