package org.edx.mobile.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.custom.SingleScrollDirectionEnforcer;

import static android.view.animation.Animation.INFINITE;
import static android.view.animation.Animation.RELATIVE_TO_SELF;


/**
 * Created by marcashman on 2014-12-02.
 */
public class UiUtil {

    public enum Animation {
        ROTATION("rotation"),
        NONE("none");

        public final String value;

        Animation(String value) {
            this.value = value;
        }
    }

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

    /**
     * Utility method to check the screen direction
     *
     * @return true if direction is LTR false else wise
     */
    public static boolean isDirectionLeftToRight() {
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
        params.leftMargin -= cardView.getPaddingStart();
        params.rightMargin -= cardView.getPaddingEnd();
        cardView.setLayoutParams(params);
    }

    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int iconResId, int resSize, int colorRes) {
        Drawable drawable = getDrawable(context, iconResId).mutate();
        setDrawableColor(context, drawable, colorRes);
        return setDrawableSize(context, drawable, resSize);
    }

    public static void setDrawableColor(@NonNull Context context, Drawable drawable, int colorRes) {
        drawable.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN);
    }

    public static Drawable setDrawableSize(Context context, Drawable drawable, int resSize) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        int size = context.getResources().getDimensionPixelSize(resSize);
        Drawable dr = DrawableCompat.wrap(new BitmapDrawable(context.getResources(), bitmap));
        dr.setBounds(0, 0, size, size);
        return dr;
    }

    public static void setAnimation(ImageView imageView, Animation animation) {
        if (animation == Animation.NONE) {
            imageView.setAnimation(null);
            return;
        }
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
                RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(INFINITE);
        anim.setDuration(1000);
        imageView.startAnimation(anim);
    }

    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int drawableId) {
        return ContextCompat.getDrawable(context, drawableId);
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
        swipeRefreshLayout.setColorSchemeResources(R.color.primaryDarkColor,
                R.color.neutralBlack);
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

    /**
     * Util method to enforce the single scrolling direction gesture of the {@link ViewPager2}, as
     * {@link ViewPager2} supports both vertical & horizontally scrolling gestures that disrupt the
     * the child list scrolling when user gesture not perfectly in single direction (e.g diagonal
     * gesture)
     *
     * @param viewPager2 view to enforce the single scrolling direction
     * @see <a href="https://medium.com/@BladeCoder/fixing-recyclerview-nested-scrolling-in-opposite-direction-f587be5c1a04">
     * Nested scrolling in Opposite Direction using ViewPager2</a>
     */
    public static void enforceSingleScrollDirection(ViewPager2 viewPager2) {
        // ViewPager2 uses a RecyclerView internally.
        RecyclerView recyclerView = (RecyclerView) viewPager2.getChildAt(0);
        if (recyclerView != null) {
            SingleScrollDirectionEnforcer enforcer = new SingleScrollDirectionEnforcer();
            recyclerView.addOnItemTouchListener(enforcer);
            recyclerView.addOnScrollListener(enforcer);
        }
    }
}
