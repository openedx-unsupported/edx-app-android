package org.edx.mobile.util

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionsUtil {
    const val WRITE_STORAGE_PERMISSION_REQUEST = 1
    const val CAMERA_PERMISSION_REQUEST = 2
    const val READ_STORAGE_PERMISSION_REQUEST = 3
    const val CALENDAR_PERMISSION_REQUEST = 4
    const val POST_NOTIFICATION_REQUEST = 5

    @JvmStatic
    fun checkPermissions(permission: String, context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(context, permission)
    }

    @JvmStatic
    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(requestCode: Int, permissions: Array<String>, fragment: Fragment) {
        fragment.requestPermissions(permissions, requestCode)
    }
}
