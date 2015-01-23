package org.edx.mobile.util;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.OEXLogger;
import org.edx.mobile.module.analytics.SegmentTracker;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class BrowserUtil {

    private static final OEXLogger logger = new OEXLogger(BrowserUtil.class.getName());

    /**
     * Opens given URL in native browser.
     * 
     * @param context
     * @param url
     */
    public static void open(Activity context, String url) {
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
                ISegment segIO = SegmentFactory.getInstance(context, 
                        new SegmentTracker(context));
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
