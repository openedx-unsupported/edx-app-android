package org.edx.mobile.view.custom.popup;

import android.graphics.drawable.Drawable;
import android.support.v7.graphics.drawable.DrawableWrapper;

public class SizedDrawable extends DrawableWrapper {
    private final int width;

    private final int height;

    public SizedDrawable(Drawable drawable, int width, int height) {
        super(drawable);
        this.width = width;
        this.height = height;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }
}
