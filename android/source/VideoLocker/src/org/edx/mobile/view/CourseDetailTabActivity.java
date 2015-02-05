package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailTabActivity extends BaseTabActivity {

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

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(android.R.color.transparent);

        setApplyPrevTransitionOnRestart(true);
        
        bundle = getIntent().getBundleExtra("bundle");
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
                    .getSerializable("enrollment");
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

    public void initializeTabs(boolean showAnnouncment) {
        /* Setup your tab icons and content views.. Nothing special in this.. */
        TabHost.TabSpec spec = tabHost
                .newTabSpec(getString(R.string.tab_chapter_list));
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);

            }
        });
        spec.setIndicator(getString(R.string.tab_label_courseware));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec(getString(R.string.tab_announcement));
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        spec.setIndicator(getString(R.string.tab_label_announcement));
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec(getString(R.string.tab_handouts));
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        spec.setIndicator(getString(R.string.tab_label_handouts));
        tabHost.addTab(spec);

        //This code has been commented to hide course info for September 15th launch

        //This handles which tab to be shown when CourseDetails is loaded 
        if (!showAnnouncment) {
            tabHost.setCurrentTabByTag(getString(R.string.tab_chapter_list));
        } else {
            tabHost.setCurrentTabByTag(getString(R.string.tab_announcement));
        }


        //Fixing the width and TextColor of a Tab
        TabWidget widget = tabHost.getTabWidget();

        for (int i = 0; i < widget.getChildCount(); i++) {
            final TextView tv = (TextView) widget.getChildAt(i).findViewById(
                    android.R.id.title);
            tv.setTextColor(this.getResources().getColorStateList(
                    R.color.tab_selector));
            tv.setSingleLine(true);
            tv.setAllCaps(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);

        if(checkBox_menuItem!=null){
            checkBox_menuItem.setVisible(false);
        }
        return true;
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
