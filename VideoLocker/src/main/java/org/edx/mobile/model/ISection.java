package org.edx.mobile.model;

import java.util.List;

/*
 * TODO: models to be refactored in GA+1
 */
interface ISection {

    ICourse getCourse();

    String getChapter();

    String getName();

    List<IVideoModel> getVideos();
}
