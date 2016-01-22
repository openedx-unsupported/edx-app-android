package org.edx.mobile.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;

/**
 * Created by marcashman on 2014-12-02.
 */
public class UiUtil {

    private static final String TAG = UiUtil.class.getCanonicalName();
    private static final Logger logger = new Logger(UiUtil.class);

    public static void showMessage(View root, String message){
        if (root == null) {
            logger.warn("cannot show message, no views available");
            return;
        }
        TextView downloadMessageTv = (TextView) root.findViewById(R.id.flying_message);
        if (downloadMessageTv != null) {
            downloadMessageTv.setText(message);
            ViewAnimationUtil.showMessageBar(downloadMessageTv);
        } else {
            logger.warn("view flying_message not found");
        }
    }

    /**
     * Call this method to inform user about going  offline
     */
    public static void showOfflineAccessMessage(View root) {
        if (root == null) {
            logger.warn("cannot show message, no views available");
            return;
        }
        try {
            ViewAnimationUtil.showMessageBar(root.findViewById(R.id.offline_access_panel));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    //Hide the offline access message
    public static void hideOfflineAccessMessage(View root) {
        if (root == null) {
            logger.warn("cannot show message, no views available");
            return;
        }
        View v = root.findViewById(R.id.offline_access_panel);
        if (v != null) {
            v.setVisibility(View.GONE);
        } else {
            logger.warn("cannot hide message, cannot find offline_access_panel");
        }
    }

    /**
     * This function is used to return the passed Value in Display Metrics form
     * @param point width/height as int
     * @return float
     */
    public static float getParamsInDP(Resources r, int point){
        try{
            float val = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, point, r.getDisplayMetrics());
            return val;
        }catch(Exception e){
            logger.error(e);
        }
        return 0;
    }


    public static boolean isLeftToRightOrientation(){
        Configuration config = MainApplication.instance().getResources().getConfiguration();
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
        } else {
            return true;
        }
    }

    /**
     * CardView adds extra padding on pre-lollipop devices for shadows
     * This function removes that extra padding from its margins
     * @param cardView The CardView that needs adjustments
     * @return float
     */
    public static void adjustCardViewMargins(View cardView) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        params.topMargin -= cardView.getPaddingTop();
        params.leftMargin -= cardView.getPaddingLeft();
        params.rightMargin -= cardView.getPaddingRight();
        cardView.setLayoutParams(params);
    }
}
