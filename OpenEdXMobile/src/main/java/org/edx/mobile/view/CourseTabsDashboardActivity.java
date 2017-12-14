package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.interfaces.SnackbarStatusListener;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.NetworkUtil;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.view.Router.EXTRA_ANNOUNCEMENTS;
import static org.edx.mobile.view.Router.EXTRA_COURSE_DATA;

public class CourseTabsDashboardActivity extends BaseSingleFragmentActivity
        implements SnackbarStatusListener, RefreshListener {
    public static Intent newIntent(@NonNull Activity activity,
                                   @NonNull EnrolledCoursesResponse courseData,
                                   boolean announcements) {
        Intent intent = new Intent(activity, CourseTabsDashboardActivity.class);
        intent.putExtra(EXTRA_COURSE_DATA, courseData);
        intent.putExtra(EXTRA_ANNOUNCEMENTS, announcements);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    private SnackbarErrorNotification snackbarErrorNotification;
    private boolean isFullScreenErrorVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
        snackbarErrorNotification = new SnackbarErrorNotification(findViewById(R.id.coordinator_layout));
    }

    @Override
    public Fragment getFirstFragment() {
        return CourseTabsDashboardFragment.newInstance();
    }

    @Override
    public void hideSnackBar() {
        snackbarErrorNotification.hideError();
    }

    @Override
    public void resetSnackbarVisibility(boolean fullScreenErrorVisibility) {
        this.isFullScreenErrorVisible = fullScreenErrorVisibility;
        final boolean isNetworkConnected = NetworkUtil.isConnected(this);
        if (fullScreenErrorVisibility || isNetworkConnected) {
            snackbarErrorNotification.hideError();
        } else if (!isNetworkConnected) {
            snackbarErrorNotification.showOfflineError(this);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (NetworkUtil.isConnected(this)) {
            snackbarErrorNotification.hideError();
        } else if (!isFullScreenErrorVisible) {
            snackbarErrorNotification.showOfflineError(this);
        }
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new CourseDashboardRefreshEvent());
    }
}
