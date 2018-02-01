package org.edx.mobile.util;

public class DownloadUtil {

    public static long getRemainingSizeToDownload(long totalSize, long downloadedSize) {
        if (totalSize <= 0 || downloadedSize > totalSize) {
            return 0; // Prevent negative values
        }
        return totalSize - downloadedSize;
    }

    public static int getPercentDownloaded(long totalSize, long downloadedSize) {
        if (totalSize <= 0) {
            return 0; // Prevent division-by-zero
        }
        return (int) (100 * downloadedSize / totalSize);
    }

    public static boolean isDownloadSizeWithinLimit(long downloadSize, long limit) {
        return downloadSize < limit;
    }
}
