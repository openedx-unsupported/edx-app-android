package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_coursedetail_tab)
public class CourseDetailTabActivity extends BaseTabActivity {

    //TODO - it is out of sync here. the logic should be in TabModel.
    private final int coursewareTabIndex = 0;
    private final int courseInfoTabIndex = 1;

    public static String TAG = CourseDetailTabActivity.class.getCanonicalName();
    static final String TAB_ID = TAG + ".tabID";

    @InjectView(R.id.offline_bar)
    private View offlineBar;

    private int selectedTab = coursewareTabIndex;

    Bundle bundle;
    String activityTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(TAB_ID);
        }

        setApplyPrevTransitionOnRestart(true);

        bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);

        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            if(offlineBar!=null){
                offlineBar.setVisibility(View.VISIBLE);
            }
        }

        try{
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(Router.EXTRA_ENROLLMENT);

            //check courseData again, it may be fetched from local cache
            if ( courseData != null ) {
                activityTitle = courseData.getCourse().getName();
                boolean isAnnouncement = bundle.getBoolean(Router.EXTRA_ANNOUNCEMENTS, false);
                if ( isAnnouncement )
                    selectedTab = courseInfoTabIndex;
                try{
                    environment.getSegment().screenViewsTracking(courseData.getCourse().getName());
                }catch(Exception e){
                    logger.error(e);
                }
            } else {

                boolean handleFromNotification = handleIntentFromNotification();
                //this is not from notification
                if (!handleFromNotification) {
                    //it is a good idea to go to the my course page. as loading of my courses
                    //take a while to load. that the only way to get anouncement link
                    environment.getRouter().showMyCourses(this);
                    finish();
                }
            }

        }catch(Exception ex){
            environment.getRouter().showMyCourses(this);
            finish();
            logger.error(ex);
        }

    }

    @Override
    protected List<TabModel> tabsToAdd() {
        List<TabModel> tabs = new ArrayList<TabModel>();
        tabs.add(new TabModel(getString(R.string.tab_label_courseware),
                CourseChapterListFragment.class,
                bundle, getString(R.string.tab_chapter_list)));
        tabs.add(new TabModel(getString(R.string.tab_label_course_info),
                CourseCombinedInfoFragment.class,
                bundle, getString(R.string.tab_course_info)));

        return tabs;
    }

    @Override
    protected int getDefaultTab() {
        return selectedTab;
    }

    protected void onStart() {
        super.onStart();
        try{
            setTitle(activityTitle);
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * @return <code>true</code> if handle intent from notification successfully
     */
    private boolean handleIntentFromNotification(){
        if ( bundle != null ){
            String courseId = bundle.getString(Router.EXTRA_COURSE_ID);
            //this is from notification
            if (!TextUtils.isEmpty(courseId)){
                try{
                    bundle.remove(Router.EXTRA_COURSE_ID);
                    ServiceManager api = environment.getServiceManager();
                    EnrolledCoursesResponse courseData = api.getCourseById(courseId);
                    if (courseData != null && courseData.getCourse() != null ) {
                        bundle.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
                        activityTitle = courseData.getCourse().getName();
                        selectedTab = courseInfoTabIndex;
                        return true;
                    }
                }catch (Exception ex){
                    logger.error(ex);
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleIntentFromNotification();
        invalidateOptionsMenu();
    }

    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOffline();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }

        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if (fragment instanceof NetworkObserver){
                ((NetworkObserver) fragment).onOnline();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAB_ID, tabHost.getCurrentTab());
    }
}
