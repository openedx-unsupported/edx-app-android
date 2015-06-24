package org.edx.mobile.base;

import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.DownloadEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.Router;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;

public abstract class MyVideosBaseFragment extends RoboFragment {
    @Inject
    protected IEdxEnvironment environment;

    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( !EventBus.getDefault().isRegistered(this) )
            EventBus.getDefault().register(this);
    }

    /**
     * used by EventBus callback
     * @param event
     */
    public void onEvent(DownloadEvent event) {
        reloadList();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    /**
     * Call this function when Video completes downloading
     * so that downloaded videos appears in MyVideos listing
     */
    public abstract void reloadList();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if ( courseData != null)
            outState.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
        if ( courseComponentId != null )
            outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_ENROLLMENT);
            courseComponentId = (String) savedInstanceState.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        }
    }
}
