package org.edx.mobile.model.download;

/**
 * This class represents a download in the native DownloadManager service.
 * @author rohan
 *
 */
public class NativeDownloadModel {

    public long downloaded;
    public long size;
    public String filepath;
    public int status;
    public long dmid;

    public int getPercent() {
        int p = (int) (100 * downloaded / size);
        return p;
    }
    
    /**
     * Returns size in MB.
     * @return
     */
    public String getSize() {
        return getMemorySize(size);
    }
    
    /**
     * Returns size of downloaded bytes in MB.
     * @return
     */
    public String getDownloaded() {
        return getMemorySize(downloaded);
    }
    
    private String getMemorySize(long bytes) {
        if (bytes == 0) {
            return "0KB";
        }
        
        long s = bytes;
        int gb = (int) (s / (1024f * 1024f * 1024f) );
        s = s % (1024 * 1024 * 1024) ;
        int mb = (int) (s / (1024f * 1024f) );
        s = s % (1024 * 1024) ;
        int kb = (int) (s / 1024f);
        int b = (int) (s % 1024);
        
        return String.format("%d MB", mb);
    }
    
    @Override
    public String toString() {
        return String.format("downloaded=%d, size=%d, path=%s", downloaded, size, filepath);
    }
}
