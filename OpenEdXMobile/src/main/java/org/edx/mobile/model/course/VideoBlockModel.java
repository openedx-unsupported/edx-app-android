package org.edx.mobile.model.course;

import android.support.annotation.Nullable;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

/**
 * common base class for all type of units
 */
public class VideoBlockModel extends CourseComponent implements HasDownloadEntry {

    private DownloadEntry downloadEntry;
    private VideoData data;
    private String downloadUrl;

    public VideoBlockModel(BlockModel blockModel, CourseComponent parent){
        super(blockModel,parent);
        this.data = (VideoData)blockModel.data;
    }

    @Nullable
    public DownloadEntry getDownloadEntry(IStorage storage) {
        if (data.encodedVideos.getPreferredVideoInfoForDownloading() == null) {
            return null;
        }
        if ( storage != null ) {
            downloadEntry = (DownloadEntry) storage
                .getDownloadEntryFromVideoModel(this);
        }
        return downloadEntry;
    }

    public void setDownloadUrl(@Nullable String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Nullable
    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public VideoData getData() {
        return data;
    }

    public void setData(VideoData data) {
        this.data = data;
    }

    /**
     * Returns the size of the video file of whichever encoding is currently preferred within the
     * app for playing or downloading.
     *
     * @return The size of the video if available, <code>-1</code> otherwise.
     */
    public long getPreferredVideoEncodingSize() {
        if (data != null && data.encodedVideos != null
                && data.encodedVideos.getPreferredVideoInfoForDownloading() != null) {
            return data.encodedVideos.getPreferredVideoInfoForDownloading().fileSize;
        }
        return -1;
    }
}
