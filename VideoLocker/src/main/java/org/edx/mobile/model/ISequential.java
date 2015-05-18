package org.edx.mobile.model;

import org.edx.mobile.model.api.VideoResponseModel;

import java.util.List;

/*
 *
 */
public interface ISequential extends IComponent{

    IChapter getChapter();

    List<IVertical> getVerticals();

    String getSectionUrl();

    void setSectionUrl(String url);

    IVertical getVerticalById(String vid);

    int getVideoCount();

    List<VideoResponseModel> getVideos();
}
