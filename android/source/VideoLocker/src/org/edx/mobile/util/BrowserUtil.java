package org.edx.mobile.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.Api;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.SegmentTracker;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.view.dialog.IDialogCallback;

public class BrowserUtil {

    private static final Logger logger = new Logger(BrowserUtil.class.getName());

    private static final String TAG = BrowserUtil.class.getCanonicalName();

    /**
     * Opens given URL in native browser.
     * 
     * @param activity
     * @param url
     */
    public static void open(final FragmentActivity activity, final String url) {

        if (TextUtils.isEmpty(url)){
            return;
        }

        String baseUrl = new Api(activity).getBaseUrl();

        if(url.indexOf(baseUrl) >= 0) {
            openInBrowser(activity, url);
        }
        else if(url.startsWith("/")) {
            openInBrowser(activity, String.format("%s%s", baseUrl, url));
        }
        else {
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

    private static void openInBrowser(FragmentActivity context, String url) {
        logger.debug(String.format("Clicking link to url: %s", url));
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);

/*          Intent intent = new Intent(context, WebviewActivity.class);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);*/
            
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
}
