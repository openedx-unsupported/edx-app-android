package org.edx.mobile.model;

import org.edx.mobile.model.api.VideoResponseModel;

import java.util.List;

/*
 * TODO: models to be refactored in GA+1
 */
public interface IChapter extends IComponent {

    ICourse getCourse();

    List<ISequential> getSequential();

    String getName();


    ISequential getSequentialById(String sid);

    int getVideoCount();

    List<VideoResponseModel> getVideos();
}
