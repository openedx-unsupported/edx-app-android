package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.task.GetCourseStructureTask;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.custom.popup.menu.PopupMenu;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 *  A base class to handle some common task
 *  NOTE- in the layout file,  these should be defined
 *  1. offlineBar
 *  2. progress_spinner
 *  3. offline_mode_message
 */
@ContentView(R.layout.activity_course_base)
public abstract  class CourseBaseActivity  extends BaseFragmentActivity implements TaskProcessCallback{

    @InjectView(R.id.offline_bar)
    View offlineBar;

    @InjectView(R.id.last_accessed_bar)
    View lastAccessBar;

    @InjectView(R.id.loading_indicator)
    ProgressBar progressWheel;

    @Inject
    CourseManager courseManager;

    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;

    private GetCourseStructureTask getHierarchyTask;

    private boolean isDestroyed;

    protected abstract String getUrlForWebView();

    protected abstract void onLoadData();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Bundle bundle = arg0;
        if ( bundle == null ) {
            if ( getIntent() != null )
                bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        }
        restore(bundle);

        blockDrawerFromOpening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getHierarchyTask != null) {
            getHierarchyTask.cancel(true);
            getHierarchyTask = null;
        }
        isDestroyed = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
    }

    protected void restore(Bundle savedInstanceState) {
        courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_COURSE_DATA);
        courseComponentId = savedInstanceState.getString(Router.EXTRA_COURSE_COMPONENT_ID);

        if (courseComponentId == null) {
            getHierarchyTask = new GetCourseStructureTask(this, courseData.getCourse().getId()) {
                @Override
                public void onSuccess(CourseComponent courseComponent) {
                    if (courseComponent != null) {
                        courseComponentId = courseComponent.getId();
                        // Only trigger the callback if the task has not been cancelled, and
                        // the Activity has not been destroyed. The task should be canceled
                        // in Activity destruction anyway, so the latter check is just a
                        // precaution.
                        if (getHierarchyTask != null && !isDestroyed) {
                            invalidateOptionsMenu();
                            onLoadData();
                            getHierarchyTask = null;
                        }
                    }
                }

                @Override
                public void onException(Exception ex) {
                    showInfoMessage(getString(R.string.no_connectivity));
                }
            };
            getHierarchyTask.setTaskProcessCallback(this);
            getHierarchyTask.setProgressDialog(progressWheel);
            getHierarchyTask.execute();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // If the data is available then trigger the callback
        // after basic initialization
        if (courseComponentId != null) {
            onLoadData();
        }
    }

    @Override
    protected void onOnline() {
        offlineBar.setVisibility(View.GONE);
        hideOfflineMessage();
    }

    @Override
    protected void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);
        hideLoadingProgress();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected boolean createOptionsMenu(Menu menu) {
        if (courseComponentId != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.course_detail, menu);
            menu.findItem(R.id.action_share_on_web).setIcon(
                    new IconDrawable(this, FontAwesomeIcons.fa_share_square_o)
                            .actionBarSize(this).colorRes(this, R.color.edx_white));
            Icon changeModeIcon = new PrefManager.UserPrefManager(this)
                    .isUserPrefVideoModel() ? FontAwesomeIcons.fa_list :
                    FontAwesomeIcons.fa_film;
            menu.findItem(R.id.action_change_mode).setIcon(
                    new IconDrawable(this, changeModeIcon)
                            .actionBarSize(this).colorRes(this, R.color.edx_white));
            return true;
        }
        return false;
    }

    @Override
    protected boolean handleOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_on_web: {
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_share_on_web),
                        Gravity.END, R.attr.edgePopupMenuStyle, R.style.edX_Widget_EdgePopupMenu);
                popup.getMenuInflater().inflate(R.menu.share_on_web, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        BrowserUtil.open(CourseBaseActivity.this, getUrlForWebView());
                        CourseComponent courseComponent = courseManager.getComponentById(
                                courseData.getCourse().getId(), courseComponentId);
                        environment.getSegment().trackOpenInBrowser(courseComponentId,
                                courseData.getCourse().getId(), courseComponent.isMultiDevice());
                        return true;
                    }
                });
                popup.show();
                return true;
            } case R.id.action_change_mode: {
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_change_mode), Gravity.END);
                popup.getMenuInflater().inflate(R.menu.change_mode, popup.getMenu());
                final PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(this);
                final MenuItem videoOnlyItem = popup.getMenu().findItem(R.id.change_mode_video_only);
                MenuItem fullCourseItem = popup.getMenu().findItem(R.id.change_mode_full_mode);
                videoOnlyItem.setIcon(new IconDrawable(this, FontAwesomeIcons.fa_film)
                        .colorRes(this, R.color.course_mode));
                fullCourseItem.setIcon(new IconDrawable(this, FontAwesomeIcons.fa_list)
                        .colorRes(this, R.color.course_mode));
                // Setting checked states
                // Only calling setChecked(true) in the selected menu item, to avoid a bug
                // in the MenuItem implementation in the framework and appcompat library
                // which causes setChecked(false) to be evaluated to setChecked(true) in
                // the case where it is part of a group with checkable behavior set to
                // 'single'. It's reported as part of another issue in
                // http://b.android.com/178709
                if (userPrefManager.isUserPrefVideoModel()) {
                    videoOnlyItem.setChecked(true);
                } else {
                    fullCourseItem.setChecked(true);
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem popupMenuItem) {
                        boolean currentVideoMode = userPrefManager.isUserPrefVideoModel();
                        boolean selectedVideoMode = videoOnlyItem == popupMenuItem;
                        if (currentVideoMode != selectedVideoMode) {
                            userPrefManager.setUserPrefVideoModel(selectedVideoMode);
                            popupMenuItem.setChecked(true);
                            Icon filterIcon = selectedVideoMode ?
                                    FontAwesomeIcons.fa_list : FontAwesomeIcons.fa_film;
                            item.setIcon(
                                    new IconDrawable(CourseBaseActivity.this, filterIcon)
                                            .actionBarSize(CourseBaseActivity.this)
                                            .colorRes(CourseBaseActivity.this, R.color.edx_white));
                            modeChanged();
                            environment.getSegment().trackCourseOutlineMode(selectedVideoMode);
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        }
        return false;
    }

    protected void modeChanged() {}

    /**
     * This function shows the offline mode message
     */
    private void showOfflineMessage(){
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the offline mode message
     */
    private void hideOfflineMessage() {
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private void showLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private void hideLoadingProgress(){
        if(progressWheel!=null){
            progressWheel.setVisibility(View.GONE);
        }
    }

    protected void hideLastAccessedView(View v) {
        try{
            lastAccessBar.setVisibility(View.GONE);
        }catch(Exception e){
            logger.error(e);
        }
    }

    protected void showLastAccessedView(View v, String title, View.OnClickListener listener) {
        lastAccessBar.setVisibility(View.VISIBLE);
        View lastAccessTextView = v == null ? findViewById(R.id.last_accessed_text) :
            v.findViewById(R.id.last_accessed_text);
        ((TextView)lastAccessTextView).setText(title);
        View detailButton = v == null ? findViewById(R.id.last_accessed_button) :
            v.findViewById(R.id.last_accessed_button);
        detailButton.setOnClickListener(listener);
    }


    /**
     * Call this function if you do not want to allow
     * opening/showing the drawer(Navigation Fragment) on swiping left to right
     */
    protected void blockDrawerFromOpening(){
        DrawerLayout drawerLayout = (DrawerLayout)
            findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    /**
     * implements TaskProcessCallback
     */
    public void startProcess(){
        showLoadingProgress();
    }
    /**
     * implements TaskProcessCallback
     */
    public void finishProcess(){
        hideLoadingProgress();
    }

    public void onMessage(@NonNull MessageType messageType, @NonNull String message){
        showErrorMessage("", message);
    }
}

