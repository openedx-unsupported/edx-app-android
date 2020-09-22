package org.edx.mobile.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

import org.edx.mobile.R;
import org.edx.mobile.event.NewRelicEvent;
import org.edx.mobile.util.PermissionsUtil;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;

public class BaseFragment extends RoboFragment {
    public interface PermissionListener {
        void onPermissionGranted(String[] permissions, int requestCode);

        void onPermissionDenied(String[] permissions, int requestCode);
    }

    private boolean isFirstVisit = true;
    protected PermissionListener permissionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
    }

    /**
     * Method to make getContext null safe
     * @return context if not null or throw exception else wise
     */
    public Context getContextOrThrow() {
        if (getContext() != null)
            return getContext();
        throw new IllegalStateException("Context can only be accessed when attached with activity");
    }

    /**
     * Method to get String argument null safe
     * @param key requested argument
     * @return argument value if found or throw exception else wise
     */
    public String getStringArgument(String key) {
        if (getArguments() != null && getArguments().getString(key) != null) {
            return getArguments().getString(key);
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
     * Defined to mock the behavior of {@link Activity#onRestart() Activity.onRestart} function.
     */
    protected void onRevisit() {
    }

    /**
     * Called when a parent activity receives a new intent in its {@link Activity#onNewIntent(Intent)
     * Activity.onNewIntent} function.
     * Defined to mock the behavior of {@link Activity#onNewIntent(Intent) Activity.onNewIntent} function.
     */
    protected void onNewIntent(Intent intent) {
    }

    /**
     * Checks the status of the provided permissions, if a permission has been given, a callback
     * function is called otherwise the said permission is requested.
     *
     * @param permissions The requested permissions.
     * @param requestCode The request code passed in {@link #requestPermissions(String[], int)}.
     */
    protected void askForPermission(String[] permissions, int requestCode) {
        if (getActivity() != null) {
            if (permissionListener != null && getGrantedPermissionsCount(permissions) == permissions.length) {
                permissionListener.onPermissionGranted(permissions, requestCode);
            } else {
                PermissionsUtil.requestPermissions(requestCode, permissions, BaseFragment.this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && getGrantedPermissionsCount(permissions) == permissions.length) {
            if (permissionListener != null) {
                permissionListener.onPermissionGranted(permissions, requestCode);
            }
        } else {
            // android.R.id.content gives you the root element of a view, without having to know its actual name/type/ID
            // Ref: https://stackoverflow.com/questions/47666685/java-lang-illegalargumentexception-no-suitable-parent-found-from-the-given-view
            Snackbar.make(getActivity().findViewById(android.R.id.content), getResources().getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show();
            if (permissionListener != null) {
                permissionListener.onPermissionDenied(permissions, requestCode);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public int getGrantedPermissionsCount(String[] permissions) {
        int grantedPermissionsCount = 0;
        for (String permission : permissions) {
            if (PermissionsUtil.checkPermissions(permission, getActivity())) {
                grantedPermissionsCount++;
            }
        }

        return grantedPermissionsCount;
    }
}
