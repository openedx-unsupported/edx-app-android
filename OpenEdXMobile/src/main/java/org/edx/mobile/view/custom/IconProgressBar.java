package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.edx.mobile.R;

/**
 * ProgressBar with convenience methods and attributes for using
 * {@link IconDrawable} as the indeterminate drawable.
 */
public class IconProgressBar extends ProgressBar {
    static {
        // Ensure that the icon font modules are registered on class load.
        Iconify.with(new FontAwesomeModule());
    }

    static final int DEFAULT_COLOR = Color.BLACK;

    @NonNull
    private ColorStateList colorStateList = ColorStateList.valueOf(DEFAULT_COLOR);

    public IconProgressBar(Context context) {
        this(context, null);
    }

    public IconProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyle);
    }

    public IconProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.IconProgressBar, defStyleAttr, 0);
        ColorStateList colorStateList = a.getColorStateList(
                R.styleable.IconProgressBar_indeterminateIconColor);
        this.colorStateList = colorStateList != null ? colorStateList :
                ColorStateList.valueOf(DEFAULT_COLOR);
        String iconKey = a.getString(R.styleable.IconProgressBar_indeterminateIconName);
        if (iconKey != null) {
            IconDrawable drawable = new IconDrawable(context, iconKey);
            if (a.getBoolean(R.styleable.IconProgressBar_indeterminateIconPulse, false)) {
                // Change animation mode to pulse without running it, as the
                // animation is controlled by the ProgressBar implementation.
                drawable.pulse();
                drawable.stop();
            }
            setIndeterminateDrawable(drawable);
        }
        a.recycle();
    }

    public void setIndeterminateIcon(@NonNull Icon icon) {
        setIndeterminateDrawable(new IconDrawable(getContext(), icon));
    }

    @Nullable
    public final Icon getIndeterminateIcon() {
        Drawable drawable = getIndeterminateDrawable();
        if (drawable instanceof IconDrawable) {
            return ((IconDrawable) drawable).getIcon();
        }
        return null;
    }

    public void setIndeterminateIconColor(@ColorInt int color) {
        setIndeterminateIconColor(ColorStateList.valueOf(color));
    }

    public void setIndeterminateIconColor(@NonNull ColorStateList colorStateList) {
        this.colorStateList = colorStateList;
        Drawable drawable = getIndeterminateDrawable();
        if (drawable instanceof IconDrawable) {
            ((IconDrawable) drawable).color(colorStateList);
        }
    }

    public void setIndeterminateIconColorResource(@ColorRes int colorResId) {
        // Since we have an @ColorRes annotation on the colorRes parameter,
        // we can be sure that we will get a non-null ColorStateList.
        //noinspection ConstantConditions,deprecation
        setIndeterminateIconColor(getContext().getResources().getColorStateList(colorResId));
    }

    @Override
    public void setIndeterminateDrawable(@Nullable Drawable drawable) {
        if (drawable instanceof IconDrawable && drawable != getIndeterminateDrawable()) {
            ((IconDrawable) drawable).color(colorStateList);
        }
        super.setIndeterminateDrawable(drawable);
    }
}
