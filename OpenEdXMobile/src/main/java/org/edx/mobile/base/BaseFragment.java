package org.edx.mobile.base;

import android.app.Activity;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import org.edx.mobile.R;
import org.edx.mobile.event.NewRelicEvent;

import de.greenrobot.event.EventBus;

import org.edx.mobile.util.PermissionsUtil;

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
            Snackbar.make(getView(), getResources().getString(R.string.permission_not_granted), Snackbar.LENGTH_LONG).show();
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
