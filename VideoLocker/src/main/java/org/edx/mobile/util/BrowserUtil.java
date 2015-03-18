package org.edx.mobile.util;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.view.dialog.IDialogCallback;

public class BrowserUtil {

    private static final Logger logger = new Logger(BrowserUtil.class.getName());

    private static final String TAG = BrowserUtil.class.getCanonicalName();

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
            String absoluteUrl = String.format("%s%s", Config.getInstance().getApiHostURL(), url);
            logger.debug(String.format("opening relative path URL: %s", absoluteUrl));
            openInBrowser(activity, absoluteUrl);
            return;
        }


        // verify if the app is running on zero-rated mobile data?
        if (NetworkUtil.isConnectedMobile(activity) && NetworkUtil.isOnZeroRatedNetwork(activity)) {

            // check if this URL is a white-listed URL, anything outside the white-list is EXTERNAL LINK
            if (ConfigUtil.isWhiteListedURL(url)) {
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
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);

            try{
                ISegment segIO = SegmentFactory.getInstance();
                segIO.trackOpenInBrowser(url);
            }catch(Exception e){
                logger.error(e);
            }

            // apply transition when user gets back from browser
            if (context instanceof BaseFragmentActivity) {
                ((BaseFragmentActivity) context).setApplyPrevTransitionOnRestart(true);
            }
            
            // apply transition animation
            context.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            logger.debug("Next transition animation applied");
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    public static boolean isUrlOfHost(String url, String host) {
        try {
            Uri uri = Uri.parse(url);
            return (uri.getHost().toString().contains(host));
        } catch(Exception ex) {
            logger.error(ex);
        }

        return false;
    }
}
