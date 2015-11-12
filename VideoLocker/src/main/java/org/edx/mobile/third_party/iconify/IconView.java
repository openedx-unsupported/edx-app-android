package org.edx.mobile.third_party.iconify;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.edx.mobile.R;

public class IconView extends ImageView {

    @NonNull
    private ColorStateList colorStateList = ColorStateList.valueOf(IconDrawable.DEFAULT_COLOR);

    public IconView(@NonNull Context context) {
        super(context);
    }

    public IconView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.IconView, defStyleAttr, 0);
        ColorStateList colorStateList = a.getColorStateList(R.styleable.IconView_iconColor);
        if (colorStateList != null) {
            this.colorStateList = colorStateList;
        }
        if (a.hasValue(R.styleable.IconView_iconName)) {
            setIcon(Iconify.IconValue.getFromHashCode(
                    a.getInt(R.styleable.IconView_iconName, -1)));
        }
        a.recycle();
    }

    public void setIcon(Iconify.IconValue icon) {
        setImageDrawable(new IconDrawable(getContext(), icon));
    }

    public final Iconify.IconValue getIcon() {
        Drawable drawable = getDrawable();
        if (drawable instanceof IconDrawable) {
            return ((IconDrawable) drawable).getIcon();
        }
        return null;
    }

    public void setIconColor(@ColorInt int color) {
        setIconColor(ColorStateList.valueOf(color));
    }

    public void setIconColor(@NonNull ColorStateList colorStateList) {
        this.colorStateList = colorStateList;
        Drawable drawable = getDrawable();
        if (drawable instanceof IconDrawable) {
            ((IconDrawable) drawable).color(colorStateList);
        }
    }

    public void setIconColorResource(@ColorRes int colorResId) {
        // Since we have an @ColorRes annotation on the colorRes parameter,
        // we can be sure that we will get a non-null ColorStateList.
        //noinspection ConstantConditions
        setIconColor(getContext().getResources().getColorStateList(colorResId));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable instanceof IconDrawable && drawable != getDrawable()) {
            ((IconDrawable) drawable).color(colorStateList);
        }
        super.setImageDrawable(drawable);
    }

    public void setRotating(boolean rotate) {
        setRotation(rotate, false);
    }

    public void setRotation(boolean rotate, boolean restart) {
        Drawable drawable = getDrawable();
        if (drawable instanceof IconDrawable) {
            IconDrawable iconDrawable = (IconDrawable) drawable;
            if (rotate) {
                iconDrawable.start();
            } else {
                iconDrawable.stop();
            }
            if (restart && getVisibility() == VISIBLE) {
                iconDrawable.setVisible(true, true);
            }
        }
    }

}
