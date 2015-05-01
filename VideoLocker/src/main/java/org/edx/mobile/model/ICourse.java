package org.edx.mobile.model;

import android.content.Context;

import org.edx.mobile.model.api.LatestUpdateModel;

import java.util.List;

/*
 * TODO: models to be refactored in GA+1
 */
public interface ICourse extends IComponent{
    LatestUpdateModel getLatestUpdateModel();

    String getStart();

    String getCourseImage();

    String getEnd();

    String getOrg();

    String getVideoOutline();

    String getNumber();

    boolean isStarted();

    boolean isEnded();

    boolean hasUpdates();

    String getCourseAbout();

    String getCourseUpdates();

    String getCourseHandout();

    String getSubscriptionId();

    public IChapter getChapterById(String cid);

    public List<IChapter> getChapters();
}
