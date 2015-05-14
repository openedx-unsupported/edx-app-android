package org.edx.mobile.model;

import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

public interface IUnit extends IComponent{
    IVertical getVertical();
    void setVertical(IVertical vertical);

    boolean isGraded();
    void setGraded(boolean graded);

    //TODO - it should not belong to here. wait for the final API design
    DownloadEntry getDownloadEntry(IStorage storage);
    void setDownloadEntry(DownloadEntry entry);

    //TODO - it should not belong to here. wait for the final API design
    VideoResponseModel getVideoResponseModel();
    void setVideoResponseModel(VideoResponseModel entry);

}
