package org.edx.mobile.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
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

    /**
     * Sets the color scheme of the provided {@link SwipeRefreshLayout}.
     *
     * @param swipeRefreshLayout The SwipeRefreshLayout to set the color scheme of.
     */
    public static void setSwipeRefreshLayoutColors(@NonNull SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeResources(R.color.edx_brand_primary_accent,
                R.color.edx_brand_gray_x_back);
    }

    /**
     * Restarts the fragment without destroying the fragment instance.
     *
     * @param fragment The fragment to restart.
     */
    public static void restartFragment(@Nullable Fragment fragment) {
        if (fragment != null) {
            fragment.getFragmentManager().beginTransaction()
                    .detach(fragment)
                    .attach(fragment).commitAllowingStateLoss();
        }
    }

    /**
     * Method to remove the child {@link Fragment} against the provided tag.
     *
     * @param parentFragment {@link Fragment} that containing the child {@link Fragment}
     * @param tag            string to search the fragment.
     */
    public static void removeFragmentByTag(@NonNull Fragment parentFragment, @NonNull String tag) {
        if (parentFragment.isAdded()) {
            final FragmentManager fragmentManager = parentFragment.getChildFragmentManager();
            final Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment)
                        .commitAllowingStateLoss();
            }
        }
    }
}
