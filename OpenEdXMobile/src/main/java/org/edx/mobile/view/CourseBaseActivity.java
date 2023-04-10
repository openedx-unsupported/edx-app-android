package org.edx.mobile.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.ActivityCourseBaseBinding;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockPath;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.util.observer.EventObserver;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.viewModel.CourseViewModel;
import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * A base class to handle some common task
 * NOTE - in the layout file,  these should be defined
 * 1. content_error_root (The layout that contains all of the following)
 * 2. content_error (The layout having the views that'll be used to show error)
 * 3. content_area (The layout having the views that'll be used to present data on screen)
 * 4. loading_indicator (A view or layout to show loading while data loads)
 */
public abstract class CourseBaseActivity extends BaseFragmentActivity
        implements TaskProcessCallback, RefreshListener {

    @Inject
    CourseAPI courseApi;

    @Inject
    CourseManager courseManager;

    protected EnrolledCoursesResponse courseData;
    protected CourseUpgradeResponse courseUpgradeData;
    protected String courseComponentId;

    private ActivityCourseBaseBinding binding;

    protected abstract void onLoadData();

    protected abstract void onCourseRefreshError(Throwable error);

    private FullScreenErrorNotification errorNotification;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        super.setToolbarAsActionBar();
        binding = ActivityCourseBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        errorNotification = new FullScreenErrorNotification(binding.contentArea);

        Bundle bundle = arg0;
        if (bundle == null) {
            if (getIntent() != null)
                bundle = getIntent().getBundleExtra(Router.EXTRA_BUNDLE);
        }
        restore(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restore(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        outState.putParcelable(Router.EXTRA_COURSE_UPGRADE_DATA, courseUpgradeData);
        outState.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            courseData = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Router.EXTRA_COURSE_DATA);
            courseUpgradeData = savedInstanceState.getParcelable(Router.EXTRA_COURSE_UPGRADE_DATA);
            courseComponentId = savedInstanceState.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        }

        if (courseComponentId == null) {
            updateCourseStructure(courseData.getCourse().getId(), null);
        }
    }

    /**
     * Method to force update the course structure from server.
     */
    protected void updateCourseStructure(String courseId, String componentId) {
        if (!environment.getLoginPrefs().isUserLoggedIn()) {
            EventBus.getDefault().post(new LogoutEvent());
            return;
        }

        CourseViewModel courseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        courseViewModel.getCourseComponent().observe(binding.getLifecycleOwner(), new EventObserver<>(courseComponent -> {
            // Check if the Course structure is updated from a specific component
            // so need to set the courseComponentId to that specific component
            // as after update app needs to show the updated content for that component.
            if (componentId != null) {
                courseComponentId = componentId;
            } else {
                courseComponentId = courseComponent.getId();
            }
            invalidateOptionsMenu();
            onLoadData();
            return null;
        }));

        courseViewModel.getHandleError().observe(binding.getLifecycleOwner(), new Observer<Throwable>() {
            @Override
            public void onChanged(Throwable throwable) {
                onCourseRefreshError(throwable);
            }
        });

        courseViewModel.getCourseData(courseId, null, false, false,
                CourseViewModel.CoursesRequestType.LIVE.INSTANCE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // If the data is available then trigger the callback
        // after basic initialization
        if (courseComponentId != null && environment.getLoginPrefs().isUserLoggedIn()) {
            onLoadData();
        }
    }

    @Override
    protected void onOffline() {
        hideLoadingProgress();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manually handle backPress button on toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function shows the loading progress wheel
     * Show progress wheel while loading the web page
     */
    private void showLoadingProgress() {
        binding.loadingIndicator.loadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * This function hides the loading progress wheel
     * Hide progress wheel after the web page completes loading
     */
    private void hideLoadingProgress() {
        binding.loadingIndicator.loadingIndicator.setVisibility(View.GONE);
    }

    /**
     * implements TaskProcessCallback
     */
    public void startProcess() {
        showLoadingProgress();
    }

    /**
     * implements TaskProcessCallback
     */
    public void finishProcess() {
        hideLoadingProgress();
    }

    public void onMessage(@NonNull MessageType messageType, @NonNull String message) {
        showErrorMessage("", message);
    }

    protected boolean isOnCourseOutline() {
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
        if (!environment.getLoginPrefs().isUserLoggedIn()) {
            EventBus.getDefault().post(new LogoutEvent());
            return;
        }
        if (isOnCourseOutline()) {
            if (getIntent() != null) {
                restore(getIntent().getBundleExtra(Router.EXTRA_BUNDLE));
            }
        } else {
            onLoadData();
        }
    }

    public ActivityCourseBaseBinding getBaseBinding() {
        return binding;
    }
}
