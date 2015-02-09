package org.edx.mobile.view;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;

public class CourseDetailTabActivity extends BaseFragmentActivity {

    /* Your Tab host */
    private TabHost mTabHost;
    private View offlineBar;
    private CourseChapterListFragment courseFragment;
    private HorizontalScrollView horizontalScrollView;

    /* Save current tabs identifier in this.. */
    private String mCurrentTab;
    Bundle bundle;
    String activityTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coursedetail_tab);

        setApplyPrevTransitionOnRestart(true);
        
        bundle = getIntent().getBundleExtra("bundle");
        offlineBar = (View) findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();
            if(offlineBar!=null){
                offlineBar.setVisibility(View.VISIBLE);
            }
        }

        boolean showAnnouncements = false;
        try{
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable("enrollment");
            activityTitle = courseData.getCourse().getName();
            try{
                segIO.screenViewsTracking(courseData.getCourse().getName());
            }catch(Exception e){
                logger.error(e);
            }
            
            try {
                showAnnouncements = bundle.getBoolean("announcemnts");
            } catch (Exception e) {
                logger.error(e);
                showAnnouncements = false;
            }
        }catch(Exception ex){
            logger.error(ex);
        }

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setOnTabChangedListener(listener);
        mTabHost.setup();

        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.tabsScroll);
        initializeTabs(showAnnouncements);
    }

    @Override
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
        TabHost.TabSpec spec = mTabHost
                .newTabSpec(getString(R.string.tab_chapter_list));
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);

            }
        });
        spec.setIndicator(getString(R.string.tab_label_courseware));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(getString(R.string.tab_announcement));
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        spec.setIndicator(getString(R.string.tab_label_announcement));
        mTabHost.addTab(spec);

        spec = mTabHost.newTabSpec(getString(R.string.tab_handouts));
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        spec.setIndicator(getString(R.string.tab_label_handouts));
        mTabHost.addTab(spec);

        //This code has been commented to hide course info for September 15th launch
        
        /*spec = mTabHost.newTabSpec(getString(R.string.tab_course_info));
        spec.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return findViewById(android.R.id.tabcontent);
            }
        });
        spec.setIndicator(getString(R.string.tab_label_course_info));
        mTabHost.addTab(spec);*/

        //This handles which tab to be shown when CourseDetails is loaded 
        if (!showAnnouncment) {
            mTabHost.setCurrentTabByTag(getString(R.string.tab_chapter_list));
            /* Set current tab.. */
            mCurrentTab = getString(R.string.tab_chapter_list);
        } else {
            mTabHost.setCurrentTabByTag(getString(R.string.tab_announcement));
            /* Set current tab.. */
            mCurrentTab = getString(R.string.tab_announcement);
        }


        //Fixing the width and TextColor of a Tab
        TabWidget widget = mTabHost.getTabWidget();
        for (int i = 0; i < widget.getChildCount(); i++) {
            final TextView tv = (TextView) widget.getChildAt(i).findViewById(
                    android.R.id.title);
            tv.setTextColor(this.getResources().getColorStateList(
                    R.color.tab_selector));
            tv.setSingleLine(true);
            tv.setAllCaps(true);
        }
    }

    /* Comes here when user switch tab, or we do programmatically */
    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        public void onTabChanged(String tabId) {

            if (tabId.equals(getString(R.string.tab_chapter_list))) {
                if(courseFragment==null){
                    courseFragment = new CourseChapterListFragment();
                    courseFragment.setArguments(bundle);
                }
                pushFragments(tabId, courseFragment);
                centerTabItem(0);
            } else if (tabId.equals(getString(R.string.tab_announcement))) {
                CourseAnnouncementFragment courseAnnouncement = new CourseAnnouncementFragment();
                courseAnnouncement.setArguments(bundle);
                pushFragments(tabId, courseAnnouncement);
                centerTabItem(1);
            } else if (tabId.equals(getString(R.string.tab_handouts))) {
                CourseHandoutFragment courseHandout = new CourseHandoutFragment();
                courseHandout.setArguments(bundle);
                pushFragments(tabId, courseHandout);
                centerTabItem(2);
            }
            //TODO : uncomment when Courseinfo tab is reactivated
            /*else if (tabId.equals(getString(R.string.tab_course_info))) {
                CourseInfoFragment courseInfo = new CourseInfoFragment();
                courseInfo.setArguments(bundle);
                pushFragments(tabId, courseInfo);
                centerTabItem(3);
            }*/

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(courseFragment!=null){
            courseFragment.showLastAccessedView(courseFragment.getView());
            courseFragment.updateList();
        }
    }


    /*
     * Might be useful if we want to switch tab programmatically, from inside
     * any of the fragment.
     */
    public void setCurrentTab(int val) {
        mTabHost.setCurrentTab(val);
    }

    //Loading fragments
    public void pushFragments(String tag, Fragment fragment) {
        try{

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();

            Fragment frag = manager.findFragmentByTag(mCurrentTab);
            if(frag!=null){
                ft.hide(frag);
                ft.commit();
            }

            /* Set current tab.. */
            mCurrentTab = tag;

            if(manager.findFragmentByTag(tag) == null){
                ft = manager.beginTransaction();
                ft.add(android.R.id.tabcontent, fragment , tag);
                ft.addToBackStack(tag);
                ft.commit();
            }
            else{
                ft = manager.beginTransaction();
                Fragment fr = manager.findFragmentByTag(tag);
                ft.show(fr);
                ft.commit();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }
        if (mCurrentTab!=null && mCurrentTab.equals(getString(R.string.tab_chapter_list))) {
            if(courseFragment!=null){
                courseFragment.fragmentOffline();
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
        if (mCurrentTab!=null && mCurrentTab.equals(getString(R.string.tab_chapter_list))) {
            if(courseFragment!=null){
                courseFragment.fragmentOnline();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    //This method centers the selected Tab
    private void centerTabItem(int position) {
        // mTabHost.setCurrentTab(position);
        final TabWidget tabWidget = mTabHost.getTabWidget();
        final int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        final int leftX = tabWidget.getChildAt(position).getLeft();
        int newX = 0;

        newX = leftX + (tabWidget.getChildAt(position).getWidth() / 2) - (screenWidth / 2);
        if (newX < 0) {
            newX = 0;
        }

        ObjectAnimator animator=ObjectAnimator.ofInt(horizontalScrollView, "scrollX",newX );
        animator.setDuration(500);
        animator.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
