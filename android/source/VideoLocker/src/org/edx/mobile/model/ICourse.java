package org.edx.mobile.model;

import android.content.Context;

import org.edx.mobile.model.api.LatestUpdateModel;

/*
 * TODO: models to be refactored in GA+1
 */
public interface ICourse {
    LatestUpdateModel getLatest_updates();

    String getStart();

    String getCourse_image(Context context);

    String getEnd();

    String getName();

    String getOrg();

    String getVideo_outline();

    String getId();

    String getNumber();

    boolean isStarted();

    boolean isEnded();

    boolean hasUpdates();

    String getCourse_about();

    String getCourse_updates();

    String getCourse_handouts();
}
