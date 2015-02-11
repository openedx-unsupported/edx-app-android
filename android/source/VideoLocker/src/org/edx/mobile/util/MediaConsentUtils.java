package org.edx.mobile.util;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.player.VideoListFragment;
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
     * Shows any needed dialogs to the user when consent is required.
     * If no consent is required, dialogCallback.onPositiveClicked is invoked directly
     *
     * This method <b>does not</b> handle the case where no network is available (offline mode)
     */
    public static void consentToMediaDownload(FragmentActivity activity, IDialogCallback consentCallback) {
        PrefManager wifiPrefManager = new PrefManager(activity, PrefManager.Pref.WIFI);
        boolean showOnDataDialog = wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true);
        boolean connectedToWifi = NetworkUtil.isConnectedWifi(activity);
        boolean connectedMobile = NetworkUtil.isConnectedMobile(activity);

        if (connectedToWifi || NetworkUtil.isOnZeroRatedNetwork(activity) || !showOnDataDialog) {
            //no consent needed, kick off download now
            consentCallback.onPositiveClicked();
        } else if (connectedMobile && showOnDataDialog) {
            showOnlyAllowingWifiDialog(activity, consentCallback);
        } else {
            logger.warn("No appropriate dialog to show. Cannot start video");
        }
    }

    private static void showDialog(FragmentActivity activity, DialogFragment dialogFragment, String tag) {
        dialogFragment.show(activity.getSupportFragmentManager(), tag);
    }

    private static void showOnlyAllowingWifiDialog(final FragmentActivity activity, final IDialogCallback listener) {
        final IDialogCallback enableMobileDownloadsListener = new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                //enable wifi before continuing
                PrefManager wifiPrefManager = new PrefManager(activity.getBaseContext(),PrefManager.Pref.WIFI);
                wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false);
                wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, false);
                listener.onPositiveClicked();
            }

            @Override
            public void onNegativeClicked() {
                listener.onNegativeClicked();
            }
        };

        String title = activity.getString(R.string.download_data_dialog_title);
        String message = activity.getString(R.string.download_data_dialog_message);

        NetworkCheckDialogFragment dialogFragment =
                NetworkCheckDialogFragment.newInstance(title, message, enableMobileDownloadsListener);

        showDialog(activity, dialogFragment, DIALOG_TAG_CONFIRM_WIFI_OFF);
    }

    public static void showLeavingAppDataDialog(final FragmentActivity activity, final IDialogCallback consentCallback){

        boolean connectedToWifi = NetworkUtil.isConnectedWifi(activity);
        if (connectedToWifi) {
            consentCallback.onPositiveClicked();
        } else {
            String title = activity.getString(R.string.leaving_app_data_title);
            String message = activity.getString(R.string.leaving_app_data_message);
            String positiveLabel = activity.getString(R.string.label_ok);
            String negativeLabel = activity.getString(R.string.label_cancel);

            NetworkCheckDialogFragment dialogFragment =
                    NetworkCheckDialogFragment.newInstance(title, message, positiveLabel, negativeLabel, consentCallback);

            showDialog(activity, dialogFragment, DIALOG_TAG_CONFIRM_LEAVING_APP);
        }
    }
}
