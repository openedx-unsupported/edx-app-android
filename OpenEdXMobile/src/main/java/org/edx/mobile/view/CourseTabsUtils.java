package org.edx.mobile.view;


import android.app.Activity;
import android.support.annotation.Nullable;

import org.edx.mobile.interfaces.SnackbarStatusListener;
import org.edx.mobile.util.NetworkUtil;

public class CourseTabsUtils {
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
        if (isVisibleToUser &&
                activity != null &&
                activity instanceof SnackbarStatusListener &&
                !NetworkUtil.isConnected(activity)) {
            ((SnackbarStatusListener) activity).resetSnackbarVisibility(isShowingFullScreenError);
        }
    }
}
