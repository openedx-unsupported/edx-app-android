package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabWidget;

import com.facebook.Session;
import com.facebook.SessionState;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class MyCoursesListActivity extends BaseTabActivity implements NetworkObserver{

    private IUiLifecycleHelper uiLifecycleHelper;
    private PrefManager featuresPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        featuresPref = new PrefManager(this, PrefManager.Pref.FEATURES);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function

        configureDrawer();

        setTitle(getString(R.string.label_my_courses));
        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        try{
            segIO.screenViewsTracking(getString(R.string.label_my_courses));
        }catch(Exception e){
            logger.error(e);
        }

        Session.StatusCallback statusCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {

                changeSocialMode(state.isOpened());
            }

        };
        uiLifecycleHelper = IUiLifecycleHelper.Factory.getInstance(this, statusCallback);
        uiLifecycleHelper.onCreate(savedInstanceState);
    }

    private void changeSocialMode(boolean socialEnabled) {

        //Social enabled is always false if social features are disabled
        boolean allowSocialPref = featuresPref.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
        if (!allowSocialPref) {
            socialEnabled = false;
        }

        if (tabHost != null) {
            TabWidget widget = tabHost.getTabWidget();
            widget.setVisibility(socialEnabled ? View.VISIBLE : View.GONE);

            if (!socialEnabled) {
                widget.setCurrentTab(0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public List<TabModel> tabsToAdd() {
        List<TabModel> tabs = new ArrayList<TabModel>();
        tabs.add(new TabModel(getString(R.string.label_my_courses),
                MyCourseListTabFragment.class,
                null, "my_course_tab_fragment"));
        tabs.add(new TabModel(getString(R.string.label_my_friends_courses),
                MyFriendsCoursesTabFragment.class,
                null, "my_friends_course_fragment"));

        return tabs;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // GetEnrolledCoursesTask();
        setTitle(getString(R.string.label_my_courses));
    }

    @Override
    public void onOffline() {
        AppConstants.offline_flag = true;
        invalidateOptionsMenu();
    }

    @Override
    public void onOnline() {
        AppConstants.offline_flag = false;
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        uiLifecycleHelper.onResume();
        changeSocialMode(new FacebookProvider().isLoggedIn());

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        uiLifecycleHelper.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiLifecycleHelper.onDestroy();
    }

    @Override
    protected int getDefaultTab() {
        return 0;
    }

    public void updateDatabaseAfterDownload(ArrayList<EnrolledCoursesResponse> list) {
        if (list != null && list.size() > 0) {
            //update all videos in the DB as Deactivated
            db.updateAllVideosAsDeactivated(dataCallback);

            for (int i = 0; i < list.size(); i++) {
                //Check if the flag of isIs_active is marked to true,
                //then activate all videos
                if (list.get(i).isIs_active()) {
                    //update all videos for a course fetched in the API as Activated
                    db.updateVideosActivatedForCourse(list.get(i).getCourse().getId(),
                            dataCallback);
                } else {
                    list.remove(i);
                }
            }

            //Delete all videos which are marked as Deactivated in the database
            storage.deleteAllUnenrolledVideos();
        }
    }

    private DataCallback<Integer> dataCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
        }
        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

    @Override
    protected void reloadMyCoursesData() {
        CourseListTabFragment fragment = (CourseListTabFragment) getFragmentByTag("my_course_tab_fragment");
        if (fragment != null) {
            fragment.loadData(false, true);
        }
    }
}
