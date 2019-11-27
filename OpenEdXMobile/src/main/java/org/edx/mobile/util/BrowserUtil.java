package org.edx.mobile.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.view.dialog.IDialogCallback;

public class BrowserUtil {

    private static final Logger logger = new Logger(BrowserUtil.class.getName());

    private static final String TAG = BrowserUtil.class.getCanonicalName();

    private BrowserUtil() {
        throw new UnsupportedOperationException();
    }

    @Inject
    private static IEdxEnvironment environment;

    /**
     * Opens given URL in native browser.
     * If app is running on zero-rated network, confirm the user if they really want to proceed
     * browsing the non-zero-rated content.
     * Otherwise, confirms the user as they are leaving the app to browse external contents.
     * 
     * @param activity
     * @param url
     */
    public static void open(final FragmentActivity activity, final String url) {
        if (TextUtils.isEmpty(url) || activity == null){
            logger.warn("cannot open URL in browser, either URL or activity parameter is NULL");
            return;
        }

        if(url.startsWith("/")) {
            // use API host as the base URL for relative paths
            String absoluteUrl = String.format("%s%s", environment.getConfig().getApiHostURL(), url);
            logger.debug(String.format("opening relative path URL: %s", absoluteUrl));
            openInBrowser(activity, absoluteUrl);
            return;
        }


        // verify if the app is running on zero-rated mobile data?
        if (NetworkUtil.isConnectedMobile(activity) && NetworkUtil.isOnZeroRatedNetwork(activity, environment.getConfig())) {

            // check if this URL is a white-listed URL, anything outside the white-list is EXTERNAL LINK
            if (ConfigUtil.Companion.isWhiteListedURL(url, environment.getConfig())) {
                // this is white-listed URL
                logger.debug(String.format("opening white-listed URL: %s", url));
                openInBrowser(activity, url);
            }
            else {
                // for non-white-listed URLs

                // inform user they may get charged for browsing this URL
                IDialogCallback callback = new IDialogCallback() {
                    @Override
                    public void onPositiveClicked() {
                        openInBrowser(activity, url);
                    }

                    @Override
                    public void onNegativeClicked() {
                    }
                };

                MediaConsentUtils.showLeavingAppDataDialog(activity, callback);
            }
        }
        else {
            logger.debug(String.format("non-zero rated network, opening URL: %s", url));
            openInBrowser(activity, url);
        }
    }

    private static void openInBrowser(FragmentActivity context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
            AnalyticsRegistry analyticsRegistry = environment.getAnalyticsRegistry();
            analyticsRegistry.trackBrowserLaunched(url);
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
