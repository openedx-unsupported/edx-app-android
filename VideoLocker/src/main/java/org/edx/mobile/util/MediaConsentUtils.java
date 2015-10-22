package org.edx.mobile.util;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.UserPrefs;
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
    private static final Logger logger = new Logger(MediaConsentUtils.class);

    /**
     * Handles playback by checking user preferences and network connectivity.
     * If the device is connected to the following, return positive callback
     * 1. WIFI
     * 2. Is connected to mobile and on zero rated network
     * 3. Is connected to mobile network and wifi preference is off
     */
    public static void consentToMediaPlayback(FragmentActivity activity, IDialogCallback consentCallback, Config config) {
        // init pref file
        UserPrefs pref = new UserPrefs(activity);
        boolean wifiPreference = pref.isDownloadOverWifiOnly();

        boolean connectedToWifi = NetworkUtil.isConnectedWifi(activity);
        boolean connectedMobile = NetworkUtil.isConnectedMobile(activity);
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(activity, config);

        if (connectedToWifi || (connectedMobile && isOnZeroRatedNetwork)
                || (connectedMobile && !wifiPreference)) {
            //no consent needed, continue playback
            consentCallback.onPositiveClicked();
        } else {
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
