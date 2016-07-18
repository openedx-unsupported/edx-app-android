package org.edx.mobile.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;

public enum CheckboxDrawableUtil {
    ;

    @NonNull
    public static Drawable createStateListDrawable(@NonNull Context context, @DimenRes int sizeRes, @ColorRes int checkedColorRes, @ColorRes int uncheckedColorRes) {
        final StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_checked}, createCheckedDrawable(context, sizeRes, checkedColorRes));
        drawable.addState(new int[]{-android.R.attr.state_checked}, createUncheckedDrawable(context, sizeRes, uncheckedColorRes));
        return drawable;
    }

    @NonNull
    public static Drawable createActionBarDrawable(@NonNull Context context, boolean isChecked) {
        return CheckboxDrawableUtil.createStaticDrawable(
                context,
                R.dimen.action_bar_icon_size,
                R.color.edx_grayscale_neutral_white,
                R.color.edx_grayscale_neutral_white,
                isChecked
        );
    }

    @NonNull
    public static Drawable createStaticDrawable(@NonNull Context context, @DimenRes int sizeRes, @ColorRes int checkedColorRes, @ColorRes int uncheckedColorRes, boolean checked) {
        return checked
                ? createCheckedDrawable(context, sizeRes, checkedColorRes)
                : createUncheckedDrawable(context, sizeRes, uncheckedColorRes);
    }

    @NonNull
    public static Drawable createCheckedDrawable(@NonNull Context context, @DimenRes int sizeRes, @ColorRes int colorRes) {
        return new IconDrawable(context, FontAwesomeIcons.fa_check_square_o)
                .sizeRes(context, sizeRes)
                .colorRes(context, colorRes);
    }

    @NonNull
    public static Drawable createUncheckedDrawable(@NonNull Context context, @DimenRes int sizeRes, @ColorRes int colorRes) {
        return new IconDrawable(context, FontAwesomeIcons.fa_square_o)
                .sizeRes(context, sizeRes)
                .colorRes(context, colorRes);
    }
}
