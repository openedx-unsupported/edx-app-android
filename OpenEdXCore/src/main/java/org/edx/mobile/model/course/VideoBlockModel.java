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

    public VideoBlockModel(BlockModel blockModel, CourseComponent parent){
        super(blockModel,parent);
        this.data = (VideoData)blockModel.data;
    }

    @Nullable
    public DownloadEntry getDownloadEntry(IStorage storage) {
        if (data.encodedVideos.getPreferredVideoInfo() == null) {
            return null;
        }
        if ( storage != null ) {
            downloadEntry = (DownloadEntry) storage
                .getDownloadEntryFromVideoModel(this);
        }
        return downloadEntry;
    }

    public VideoData getData() {
        return data;
    }

    public void setData(VideoData data) {
        this.data = data;
    }


}
