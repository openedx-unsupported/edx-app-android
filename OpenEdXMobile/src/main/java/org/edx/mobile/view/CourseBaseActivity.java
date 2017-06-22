package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

import retrofit2.Call;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 *  A base class to handle some common task
 *  NOTE - in the layout file,  these should be defined
 *  1. content_error_root (The layout that contains all of the following)
 *  2. content_error (The layout having the views that'll be used to show error)
 *  3. content_area (The layout having the views that'll be used to present data on screen)
 *  4. loading_indicator (A view or layout to show loading while data loads)
 */
@ContentView(R.layout.activity_course_base)
public abstract  class CourseBaseActivity  extends BaseFragmentActivity
        implements TaskProcessCallback, RefreshListener{

    @InjectView(R.id.last_accessed_bar)
    View lastAccessBar;

    @InjectView(R.id.loading_indicator)
    ProgressBar progressWheel;

    @InjectView(R.id.content_area)
    ViewGroup contentLayout;

    @Inject
    CourseAPI courseApi;

    @Inject
    CourseManager courseManager;

    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;

    private Call<CourseStructureV1Model> getHierarchyCall;

    protected abstract void onLoadData();

    private FullScreenErrorNotification errorNotification;

    private SnackbarErrorNotification snackbarErrorNotification;

    // Reason of usage: Helps in deciding if we want to show a full screen error or a SnackBar.
    private boolean isInitialServerCallDone = false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setIcon(android.R.color.transparent);
        }
        errorNotification = new FullScreenErrorNotification(contentLayout);
        snackbarErrorNotification = new SnackbarErrorNotification(contentLayout);

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
        if (getHierarchyCall != null) {
            getHierarchyCall.cancel();
            getHierarchyCall = null;
        }
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
            final String courseId = courseData.getCourse().getId();
            getHierarchyCall = courseApi.getCourseStructure(courseId);
            getHierarchyCall.enqueue(new CourseAPI.GetCourseStructureCallback(this, courseId,
                    new ProgressViewController(progressWheel), errorNotification,
                    snackbarErrorNotification, this) {
                @Override
                protected void onResponse(@NonNull final CourseComponent courseComponent) {
                    courseComponentId = courseComponent.getId();
                    invalidateOptionsMenu();
                    onLoadData();
                }

                @Override
                protected void onFinish() {
                    isInitialServerCallDone = true;
                }
            });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // If the data is available then trigger the callback
        // after basic initialization
        if (courseComponentId != null) {
            onLoadData();
            isInitialServerCallDone = true;
        }
    }

    @Override
    protected void onOffline() {
        hideLoadingProgress();
        if (isInitialServerCallDone && !errorNotification.isShowing()) {
            snackbarErrorNotification.showOfflineError(CourseBaseActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    protected boolean isOnCourseOutline(){
        if (courseComponentId == null) return true;
        CourseComponent outlineComp = courseManager.getComponentById(
                courseData.getCourse().getId(), courseComponentId);
        BlockPath outlinePath = outlineComp.getPath();
        int outlinePathSize = outlinePath.getPath().size();

        return outlinePathSize <= 1;
    }

    @Override
    public void onRefresh() {
        errorNotification.hideError();
        if (isOnCourseOutline()) {
            if (getIntent() != null) {
                restore(getIntent().getBundleExtra(Router.EXTRA_BUNDLE));
            }
        } else {
            onLoadData();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (NetworkUtil.isConnected(this)) {
            snackbarErrorNotification.hideError();
        }
    }
}

