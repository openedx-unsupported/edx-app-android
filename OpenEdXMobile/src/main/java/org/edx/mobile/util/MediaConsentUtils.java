package org.edx.mobile.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.NetworkCheckDialogFragment;

/**
 * Created by marcashman on 2014-12-04.
 */
public class MediaConsentUtils {

    private static final String TAG = MediaConsentUtils.class.getCanonicalName();
    public static final String DIALOG_TAG_CONFIRM_MOBILE_DATA = TAG + ".confirm";
    public static final String DIALOG_TAG_CONFIRM_WIFI_OFF = TAG + ".wifioff";
    public static final String DIALOG_TAG_CONFIRM_LEAVING_APP = TAG + ".leaving";

    @Inject
    private static Config config;

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
                return !MainApplication.getEnvironment(context).getUserPrefs().isDownloadOverWifiOnly() ||
                        NetworkUtil.isOnZeroRatedNetwork(context, config);
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

    private static void showDialog(FragmentActivity activity, DialogFragment dialogFragment, String tag) {
        dialogFragment.show(activity.getSupportFragmentManager(), tag);
    }

    public static void showLeavingAppDataDialog(final FragmentActivity activity, final IDialogCallback consentCallback){

        boolean connectedToWifi = NetworkUtil.isConnectedWifi(activity);
        if (connectedToWifi) {
            consentCallback.onPositiveClicked();
        } else {
            CharSequence platformName = activity.getString(R.string.platform_name);
            String title = ResourceUtil.getFormattedString(activity.getResources(), R.string.leaving_app_data_title, "platform_name", platformName).toString();
            String message = ResourceUtil.getFormattedString(activity.getResources(), R.string.leaving_app_data_message, "platform_name", platformName).toString();
            String positiveLabel = activity.getString(R.string.label_ok);
            String negativeLabel = activity.getString(R.string.label_cancel);

            NetworkCheckDialogFragment dialogFragment =
                    NetworkCheckDialogFragment.newInstance(title, message, positiveLabel, negativeLabel, consentCallback);

            showDialog(activity, dialogFragment, DIALOG_TAG_CONFIRM_LEAVING_APP);
        }
    }
}
