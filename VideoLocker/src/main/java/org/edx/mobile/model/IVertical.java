package org.edx.mobile.model;


import org.edx.mobile.model.api.VideoResponseModel;

import java.util.List;

public interface IVertical extends IComponent{
    ISequential getSequential();

    List<IUnit> getUnits();

    void setUnitUrl(String url);
    String getUnitUrl();

    int getVideoCount();

    List<VideoResponseModel> getVideos();
}
