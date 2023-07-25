package org.edx.mobile.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.fragment.app.FragmentActivity;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.view.dialog.IDialogCallback;

/**
 * Created by marcashman on 2014-12-04.
 */
public class MediaConsentUtils {

    private static final String TAG = MediaConsentUtils.class.getCanonicalName();

    /**
     * Returns true if media can be streamed on the active network
     * without requiring user consent.
     */
    public static boolean canStreamMedia(Context context) {
        NetworkInfo info = NetworkUtil.getNetworkInfo(context);
        if (info == null || !info.isConnected()) return false;
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_BLUETOOTH:
            case ConnectivityManager.TYPE_ETHERNET:
                return true;
            default:
                return !MainApplication.getEnvironment(context).getUserPrefs().isDownloadOverWifiOnly();
        }
    }

    /**
     * Verifies user consent to media streaming on the active network.
     */
    public static void requestStreamMedia(FragmentActivity activity, IDialogCallback consentCallback) {
        if (canStreamMedia(activity)) {
            // No consent required, initiate streaming.
            consentCallback.onPositiveClicked();
        } else {
            // No dialog for user consent implemented at
            // the moment, so just show the error messages.
            consentCallback.onNegativeClicked();
        }
    }
}
