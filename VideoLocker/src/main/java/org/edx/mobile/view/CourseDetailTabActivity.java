package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailTabActivity extends BaseTabActivity {

    public static final String EXTRA_ANNOUNCEMENTS = "announcemnts";
    public static final String EXTRA_BUNDLE = "bundle";
    public static String TAG = CourseDetailTabActivity.class.getCanonicalName();
    static final String TAB_ID = TAG + ".tabID";

    private View offlineBar;

    private int selectedTab = 0;

    Bundle bundle;
    String activityTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coursedetail_tab);

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(TAB_ID);
        }

        setApplyPrevTransitionOnRestart(true);
        
        bundle = getIntent().getBundleExtra(EXTRA_BUNDLE);
        offlineBar = findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            if(offlineBar!=null){
                offlineBar.setVisibility(View.VISIBLE);
            }
        }

        try{
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(BaseFragmentActivity.EXTRA_ENROLLMENT);
            activityTitle = courseData.getCourse().getName();
            try{
                segIO.screenViewsTracking(courseData.getCourse().getName());
            }catch(Exception e){
                logger.error(e);
            }

        }catch(Exception ex){
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

    @Override
    protected void onResume() {
        super.onResume();
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
