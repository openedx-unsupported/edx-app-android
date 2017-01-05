package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;

import retrofit2.Call;
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
    CourseAPI courseApi;

    @Inject
    CourseManager courseManager;

    protected EnrolledCoursesResponse courseData;
    protected String courseComponentId;

    private Call<CourseStructureV1Model> getHierarchyCall;

    protected abstract void onLoadData();

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
                    CallTrigger.LOADING_CACHED, new ProgressViewController(progressWheel)) {
                @Override
                protected void onResponse(@NonNull final CourseComponent courseComponent) {
                    courseComponentId = courseComponent.getId();
                    invalidateOptionsMenu();
                    onLoadData();
                }

                @Override
                public void onFailure(@NonNull final Call<CourseStructureV1Model> call,
                                      @NonNull final Throwable error) {
                    showInfoMessage(getString(R.string.no_connectivity));
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

