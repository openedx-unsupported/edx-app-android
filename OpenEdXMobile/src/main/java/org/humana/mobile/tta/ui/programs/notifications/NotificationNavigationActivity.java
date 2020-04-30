package org.humana.mobile.tta.ui.programs.notifications;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.humana.mobile.R;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.programs.notifications.viewModel.NotificationNavigationViewModel;

public class NotificationNavigationActivity extends BaseVMActivity {
    private NotificationNavigationViewModel viewModel;
    private EnrolledCoursesResponse course;
    private String courseId, unitId;
    private String notificationId, userName;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            getBundledData(getIntent().getExtras());
        } else if (savedInstanceState != null) {
            getBundledData(savedInstanceState);
        }


        savedInstanceState = getIntent().getExtras();
        assert savedInstanceState != null;

        viewModel = new NotificationNavigationViewModel(this, courseId, unitId, notificationId, userName);
        binding(R.layout.t_activty_notification_navigation, viewModel);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (courseId != null) {
            outState.putString(Constants.KEY_ACTION_PARENT_ID, courseId);
        }
        if (unitId != null) {
            outState.putSerializable(Constants.KEY_ACTION_ID, unitId);
        }
        if (notificationId != null) {
            outState.putSerializable(Constants.KEY_NOTIFICATION_ID, notificationId);
        }
        if (userName != null) {
            outState.putSerializable(Constants.EXTRA_ACTION_USERNAME, userName);
        }
    }

    private void getBundledData(Bundle parameters) {
        if (parameters.containsKey(Constants.KEY_ACTION_PARENT_ID)) {
            courseId = parameters.getString(Constants.KEY_ACTION_PARENT_ID);

        }
        if (parameters.containsKey(Constants.KEY_ACTION_ID)) {
            unitId = parameters.getString(Constants.KEY_ACTION_ID);
        }

        if (parameters.containsKey(Constants.KEY_NOTIFICATION_ID)) {
            notificationId = parameters.getString(Constants.KEY_NOTIFICATION_ID);
        }
        if (parameters.containsKey(Constants.EXTRA_ACTION_USERNAME)) {
            userName = parameters.getString(Constants.EXTRA_ACTION_USERNAME);
        }


    }
}