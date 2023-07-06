package org.edx.mobile.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.view.dialog.AlertDialogFragment;

public class BrowserUtil {

    private static final Logger logger = new Logger(BrowserUtil.class.getName());

    private static final String TAG = BrowserUtil.class.getCanonicalName();

    private BrowserUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method to show an alert dialog to warn the user about opening the URL in an external browser
     *
     * @param activity      The reference of the activity displaying the dialog.
     * @param platformName  User facing name of the platform.
     * @param url           The URL to load.
     * @param canTrackEvent Flag to track analytics event.
     */
    public static void showOpenInBrowserDialog(final FragmentActivity activity, final String platformName,
                                               final String url, AnalyticsRegistry analyticsRegistry,
                                               final boolean canTrackEvent) {
        if (TextUtils.isEmpty(url) || activity == null) {
            logger.warn("cannot open URL in browser, either URL or activity parameter is NULL");
            return;
        }
        String title = activity.getString(R.string.label_leaving_the_app);
        String msg = ResourceUtil.getFormattedString(activity.getResources(),
                        R.string.leaving_the_app_message, AppConstants.PLATFORM_NAME, platformName)
                .toString();
        String positiveBtn = activity.getString(R.string.label_continue);
        String negativeBtn = activity.getString(R.string.label_cancel);
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(title, msg,
                positiveBtn, (dialog, which) -> {
                    analyticsRegistry.trackOpenInBrowserAlertActionTaken(url, Analytics.Values.ACTION_CONTINUE);
                    open(activity, url, canTrackEvent);
                },
                negativeBtn, (dialog, which) -> {
                    analyticsRegistry.trackOpenInBrowserAlertActionTaken(url, Analytics.Values.ACTION_CANCEL);
                });

        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(alertDialog, "")
                .commitAllowingStateLoss();

        analyticsRegistry.trackOpenInBrowserAlertTriggerEvent(url);
    }

    /**
     * Opens given URL in native browser.
     * If app is running on zero-rated network, confirm the user if they really want to proceed
     * browsing the non-zero-rated content.
     * Otherwise, confirms the user as they are leaving the app to browse external contents.
     *
     * @param activity
     * @param url
     * @param canTrackEvent
     */
    public static void open(final FragmentActivity activity, final String url, final boolean canTrackEvent) {
        if (TextUtils.isEmpty(url) || activity == null) {
            logger.warn("cannot open URL in browser, either URL or activity parameter is NULL");
            return;
        }
        Config config = MainApplication.getEnvironment(activity).getConfig();
        if (url.startsWith("/")) {
            // use API host as the base URL for relative paths
            String absoluteUrl = String.format("%s%s", config.getApiHostURL(), url);
            logger.debug(String.format("opening relative path URL: %s", absoluteUrl));
            openInBrowser(activity, absoluteUrl, canTrackEvent);
            return;
        }
        openInBrowser(activity, url, canTrackEvent);
    }

    private static void openInBrowser(FragmentActivity context, String url, boolean canTrackEvent) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
            if (canTrackEvent) {
                AnalyticsRegistry analyticsRegistry = MainApplication.getEnvironment(context).getAnalyticsRegistry();
                analyticsRegistry.trackBrowserLaunched(url);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.cannot_open_url, Toast.LENGTH_SHORT).show();

            // Send non-fatal exception
            logger.error(new Exception(String.format("No activity found (browser cannot handle request) for this url: %s, error:\n", url)
                    + e.getMessage()), true);
        }
    }

    public static boolean isUrlOfHost(String url, String host) {
        if (url != null && host != null) {
            String urlHost = Uri.parse(url).getHost();
            if (urlHost != null) {
                return urlHost.matches("^(.+\\.)?" + host + "$");
            }
        }
        return false;
    }
}
