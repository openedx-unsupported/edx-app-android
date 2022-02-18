package org.edx.mobile.view

import android.app.Activity
import org.edx.mobile.base.BaseAppActivity
import org.edx.mobile.interfaces.SnackbarStatusListener
import org.edx.mobile.util.NetworkUtil

object OfflineSupportUtils {
    @JvmStatic
    fun setUserVisibleHint(
        activity: Activity?, isVisibleToUser: Boolean,
        isShowingFullScreenError: Boolean
    ) {
        if (isVisibleToUser &&
            activity is SnackbarStatusListener
        ) {
            (activity as SnackbarStatusListener).resetSnackbarVisibility(isShowingFullScreenError)
        }
    }

    @JvmStatic
    fun onRevisit(activity: Activity?) {
        if (activity is SnackbarStatusListener &&
            NetworkUtil.isConnected(activity)
        ) {
            (activity as SnackbarStatusListener).hideSnackBar()
        }
    }

    @JvmStatic
    fun onNetworkConnectivityChangeEvent(
        activity: Activity?,
        isVisibleToUser: Boolean,
        isShowingFullScreenError: Boolean
    ) {

        /*
         This event is consumed even when the activity isn't in foreground.
         We need to ensure that the following logic only executes when the activity and fragment
         are in the foreground.
         */
        val isActivityVisible: Boolean
        run {
            isActivityVisible = if (activity is BaseAppActivity) {
                activity.isInForeground
            } else {
                false
            }
        }
        if (isActivityVisible &&
            isVisibleToUser &&
            activity is SnackbarStatusListener &&
            !NetworkUtil.isConnected(activity)
        ) {
            (activity as SnackbarStatusListener).resetSnackbarVisibility(isShowingFullScreenError)
        }
    }
}
