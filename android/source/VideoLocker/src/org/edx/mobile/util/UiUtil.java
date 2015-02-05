package org.edx.mobile.util;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;

/**
 * Created by marcashman on 2014-12-02.
 */
public class UiUtil {

    private static final String TAG = UiUtil.class.getCanonicalName();

    public static void animateLayouts(View view){
        if (view == null) {
            new Logger(UiUtil.class.getSimpleName()).error(new Exception("null view cannot be animated!"));
            return;
        }
        LayoutAnimationControllerUtil messageController;
        messageController = new LayoutAnimationControllerUtil(view);
        messageController.showMessageBar();
    }

    public static void stopAnimation(View view){
        if (view != null) {
            LayoutAnimationControllerUtil messageController;
            messageController = new LayoutAnimationControllerUtil(view);
            messageController.stopAnimation();
        }
    }

    public static void showMessage(View root, String message){
        if (root == null) {
            Log.w(TAG, "cannot show message, no views available");
            return;
        }
        TextView downloadMessageTv = (TextView) root.findViewById(R.id.downloadMessage);
        if (downloadMessageTv != null) {
            downloadMessageTv.setText(message);
            animateLayouts(downloadMessageTv);
        } else {
            Log.w(TAG, "view downloadMessage not found");
        }
    }

    /**
     * Call this method to inform user about going  offline
     */
    public static void showOfflineAccessMessage(View root) {
        if (root == null) {
            Log.w(TAG, "cannot show message, no views available");
            return;
        }
        try {
            animateLayouts(root.findViewById(R.id.offline_access_panel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Hide the offline access message
    public static void hideOfflineAccessMessage(View root) {
        if (root == null) {
            Log.w(TAG, "cannot show message, no views available");
            return;
        }
        View v = root.findViewById(R.id.offline_access_panel);
        if (v != null) {
            v.setVisibility(View.GONE);
        } else {
            Log.w(TAG, "cannot hide message, cannot find offline_access_panel");
        }
    }
}
