package org.edx.mobile.model;

import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

/**
 * common base class for all type of units
 */
public abstract class UnitBaseModel extends CourseComponent implements IUnit {
    private IVertical vertical;

    private DownloadEntry downloadEntry;
    private boolean graded;

    public UnitBaseModel(IVertical vertical, String name){
        this.vertical = vertical;
        this.setName( name );
    }

    @Override
    public IVertical getVertical() {
        return vertical;
    }

    @Override
    public void setVertical(IVertical vertical) {
        this.vertical = vertical;
    }

    @Override
    public boolean isGraded(){
        return graded;
    }

    @Override
    public void setGraded(boolean graded){
        this.graded = graded;
    }

    @Override
    public DownloadEntry getDownloadEntry(IStorage storage) {
        if ( downloadEntry != null )
            return downloadEntry;
        if ( videoResponseModel != null && storage != null ) {
            downloadEntry = (DownloadEntry) storage
                .getDownloadEntryfromVideoResponseModel(videoResponseModel);
        }
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
