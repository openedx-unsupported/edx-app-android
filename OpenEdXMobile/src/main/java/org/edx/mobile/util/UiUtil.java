package org.edx.mobile.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by marcashman on 2014-12-02.
 */
public class UiUtil {

    private static final String TAG = UiUtil.class.getCanonicalName();
    private static final Logger logger = new Logger(UiUtil.class);

    public static void showMessage(View root, String message) {
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
     * This function is used to return the passed Value in Display Metrics form
     *
     * @param point width/height as int
     * @return float
     */
    public static float getParamsInDP(Resources r, int point) {
        try {
            float val = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, point, r.getDisplayMetrics());
            return val;
        } catch (Exception e) {
            logger.error(e);
        }
        return 0;
    }


    public static boolean isLeftToRightOrientation() {
        Configuration config = MainApplication.instance().getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
        } else {
            return true;
        }
    }

    /**
     * CardView adds extra padding on pre-lollipop devices for shadows
     * This function removes that extra padding from its margins
     *
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

    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return context.getDrawable(drawableId);

        //noinspection deprecation
        return context.getResources().getDrawable(drawableId);
    }

    @DrawableRes
    public static int getDrawable(@NonNull Context context, @NonNull String drawableName) {
        return context.getResources().getIdentifier(drawableName, "drawable",
                context.getPackageName());
    }

    @RawRes
    public static int getRawFile(@NonNull Context context, @NonNull String fileName) {
        return context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
    }


    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generates a unique ID for a view.
     * <br/>
     * Inspiration: https://stackoverflow.com/a/25855295/1402616
     *
     * @return View ID.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    /**
     * Sets the color scheme of the provided {@link SwipeRefreshLayout}.
     *
     * @param swipeRefreshLayout The SwipeRefreshLayout to set the color scheme of.
     */
    public static void setSwipeRefreshLayoutColors(@NonNull SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeResources(R.color.edx_brand_primary_accent,
                R.color.edx_brand_gray_x_back);
    }
}
