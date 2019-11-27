package org.edx.mobile.view;


import android.app.Activity;
import androidx.annotation.Nullable;

import org.edx.mobile.base.RoboAppCompatActivity;
import org.edx.mobile.interfaces.SnackbarStatusListener;
import org.edx.mobile.util.NetworkUtil;

public class OfflineSupportUtils {
    public static void setUserVisibleHint(@Nullable Activity activity, boolean isVisibleToUser,
                                          boolean isShowingFullScreenError) {
        if (isVisibleToUser &&
                activity != null &&
                activity instanceof SnackbarStatusListener) {
            ((SnackbarStatusListener) activity).resetSnackbarVisibility(isShowingFullScreenError);
        }
    }

    public static void onRevisit(@Nullable Activity activity) {
        if (activity != null &&
                activity instanceof SnackbarStatusListener &&
                NetworkUtil.isConnected(activity)) {
            ((SnackbarStatusListener) activity).hideSnackBar();
        }
    }

    public static void onNetworkConnectivityChangeEvent(@Nullable Activity activity,
                                                        boolean isVisibleToUser,
                                                        boolean isShowingFullScreenError) {

        /*
         This event is consumed even when the activity isn't in foreground.
         We need to ensure that the following logic only executes when the activity and fragment
         are in the foreground.
         */
        final boolean isActivityVisible;
        {
            if (activity != null && activity instanceof RoboAppCompatActivity) {
                isActivityVisible = ((RoboAppCompatActivity) activity).isInForeground();
            } else {
                isActivityVisible = false;
            }
        }
        if (isActivityVisible &&
                isVisibleToUser &&
                activity instanceof SnackbarStatusListener &&
                !NetworkUtil.isConnected(activity)) {
            ((SnackbarStatusListener) activity).resetSnackbarVisibility(isShowingFullScreenError);
        }
    }
}
