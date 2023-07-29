package org.edx.mobile.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.edx.mobile.R;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;

public class BaseFragment extends Fragment {

    private boolean isFirstVisit = true;

    protected ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    showPermissionDeniedMessage();
                }
            });

    protected void showPermissionDeniedMessage() {
        Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                getResources().getString(R.string.permission_not_granted),
                Snackbar.LENGTH_LONG
        ).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Method to make getContext null safe
     *
     * @return context if not null or throw exception else wise
     */
    public Context getContextOrThrow() {
        if (getContext() != null)
            return getContext();
        throw new IllegalStateException("Context can only be accessed when attached with activity");
    }

    /**
     * Method to get String argument null safe
     *
     * @param key requested argument
     * @return argument value if found or throw exception else wise
     */
    public String getStringArgument(String key) {
        if (getArguments() != null && getArguments().getString(key) != null) {
            return getArguments().getString(key);
        }
        throw new IllegalArgumentException("Arguments or key not found in bundle");
    }

    /**
     * Method to get Boolean argument null safe
     *
     * @param key requested argument
     * @return argument value if found or throw exception else wise
     */
    public boolean getBooleanArgument(String key, boolean defaultValue) {
        if (getArguments() != null) {
            return getArguments().getBoolean(key, defaultValue);
        }
        throw new IllegalArgumentException("Arguments or key not found in bundle");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstVisit) {
            isFirstVisit = false;
        } else {
            onRevisit();
        }
    }

    /**
     * Called when a Fragment is re-displayed to the user (the user has navigated back to it).
     * Defined to mock the behavior of {@link `Activity#onRestart()` Activity.onRestart} function.
     */
    protected void onRevisit() {
    }

    /**
     * Called when a parent activity receives a new intent in its {@link `Activity#onNewIntent(Intent)`
     * Activity.onNewIntent} function.
     * Defined to mock the behavior of {@link `Activity#onNewIntent(Intent)` Activity.onNewIntent} function.
     */
    protected void onNewIntent(Intent intent) {
    }

    public void showCalendarRemovedSnackbar() {
        SnackbarErrorNotification snackbarErrorNotification = new SnackbarErrorNotification(requireView());
        snackbarErrorNotification.showError(R.string.message_after_course_calendar_removed,
                0, R.string.label_close, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, v -> snackbarErrorNotification.hideError());
    }

    public void showCalendarUpdatedSnackbar() {
        SnackbarErrorNotification snackbarErrorNotification = new SnackbarErrorNotification(requireView());
        snackbarErrorNotification.showError(R.string.message_after_course_calendar_updated,
                0, R.string.label_close, SnackbarErrorNotification.COURSE_DATE_MESSAGE_DURATION, v -> snackbarErrorNotification.hideError());
    }

    /**
     * Returns true if current orientation is LANDSCAPE, false otherwise.
     */
    protected boolean isLandscape() {
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }
}
