package org.edx.mobile.third_party.iconify;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import org.edx.mobile.R;

public class IconButton extends ImageButton {

    private int color;

    public IconButton(Context context) {
        super(context);
    }

    public IconButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.iconButtonStyle);
    }

    public IconButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.IconView, defStyleAttr, 0);
        color = a.getColor(R.styleable.IconView_iconColor, Color.BLACK);
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

    public void setIconColor(int iconColor) {
        color = iconColor;
        Drawable drawable = getDrawable();
        if (drawable instanceof IconDrawable) {
            ((IconDrawable) drawable).color(color);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable instanceof IconDrawable && drawable != getDrawable()) {
            ((IconDrawable) drawable).color(color);
        }
        super.setImageDrawable(drawable);
    }

}
