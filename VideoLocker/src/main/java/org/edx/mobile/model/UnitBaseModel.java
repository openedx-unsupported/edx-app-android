package org.edx.mobile.model;

import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;

/**
 * common base class for all type of units
 */
public abstract class UnitBaseModel extends CourseComponent implements IUnit {
    private IVertical vertical;

    private DownloadEntry downloadEntry;

    public UnitBaseModel(IVertical vertical, String name){
        this.vertical = vertical;
        this.setName( name );
    }

    public IVertical getVertical() {
        return vertical;
    }

    public void setVertical(IVertical vertical) {
        this.vertical = vertical;
    }

    @Override
    public DownloadEntry getDownloadEntry() {
        return downloadEntry;
    }

    @Override
    public void setDownloadEntry(DownloadEntry downloadEntry) {
        this.downloadEntry = downloadEntry;
    }


    private VideoResponseModel videoResponseModel;
    public VideoResponseModel getVideoResponseModel(){
        return videoResponseModel;
    }

    public void setVideoResponseModel(VideoResponseModel entry){
        this.videoResponseModel =entry;
    }
}
