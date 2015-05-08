package org.edx.mobile.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.event.DownloadCompleteEvent;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.view.Router;

import de.greenrobot.event.EventBus;

public abstract class MyVideosBaseFragment extends Fragment {
    protected IDatabase db;
    protected IStorage storage;
    protected ISegment segIO;
    protected EnrolledCoursesResponse courseData;
    protected ICourse course;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDB();
        EventBus.getDefault().register(this);
    }

    /**
     * used by EventBus callback
     * @param event
     */
    public void onEvent(DownloadCompleteEvent event) {
        reloadList();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initDB() {
        storage = new Storage(getActivity());

        UserPrefs userprefs = new UserPrefs(getActivity());
        String username = null;
        if (userprefs != null) {
            ProfileModel profile = userprefs.getProfile();
            if(profile!=null){
                username =profile.username;
            }
        }
        db = DatabaseFactory.getInstance( DatabaseFactory.TYPE_DATABASE_NATIVE );
        
        segIO = SegmentFactory.getInstance();
    }

    /**
     * Call this function when Video completes downloading
     * so that downloaded videos appears in MyVideos listing
     */
    public abstract void reloadList();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if ( courseData != null)
            outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        if ( course != null )
            outState.putSerializable(Router.EXTRA_COURSE, course);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_COURSE_DATA);
            course = (ICourse) savedInstanceState.getSerializable(Router.EXTRA_COURSE);
        }
    }
}
