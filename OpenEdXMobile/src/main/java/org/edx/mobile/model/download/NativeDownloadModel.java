package org.edx.mobile.model.download;

/**
 * This class represents a download in the native DownloadManager service.
 */
public class NativeDownloadModel {

    public int downloadCount;
    public long downloaded;
    public long size; // size might be -1 if download has not yet started, or 0 if it has failed
    public String filepath;
    public int status;
    public long dmid;

    public int getPercentDownloaded() {
        if (size <= 0) {
            return 0; // Prevent division-by-zero
        }
        return (int) (100 * downloaded / size);
    }

    @Override
    public String toString() {
        return String.format("downloaded=%d, size=%d, path=%s", downloaded, size, filepath);
    }
}
