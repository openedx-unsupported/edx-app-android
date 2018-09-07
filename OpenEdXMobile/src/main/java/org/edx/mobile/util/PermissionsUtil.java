package org.edx.mobile.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

public class PermissionsUtil {
    public static final int WRITE_STORAGE_PERMISSION_REQUEST = 1;
    public static final int CAMERA_PERMISSION_REQUEST = 2;
    public static final int READ_STORAGE_PERMISSION_REQUEST = 3;

    public static boolean checkPermissions(String permission, @NonNull Activity activity) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissions(int requestCode, @NonNull String[] permissions, @NonNull Fragment fragment) {
        fragment.requestPermissions(permissions, requestCode);
    }
}
