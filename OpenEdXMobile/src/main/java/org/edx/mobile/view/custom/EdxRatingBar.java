package org.edx.mobile.view.custom;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.RatingBar;

import org.edx.mobile.R;

public class EdxRatingBar extends RatingBar {
    @ColorInt
    private final int UNSELECTED_STAR_COLOR = ContextCompat.getColor(getContext(), R.color.edx_brand_gray_back);
    @ColorInt
    private final int SELECTED_STAR_COLOR = ContextCompat.getColor(getContext(), R.color.edx_brand_primary_base);
    @ColorInt
    private final int SELECTED_STAR_COLOR_DARK = ContextCompat.getColor(getContext(), R.color.edx_brand_primary_dark);

    public EdxRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public EdxRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EdxRatingBar(Context context) {
        super(context);
        init();
    }

    private void init() {
        // Change colors of stars
        LayerDrawable stars = (LayerDrawable) this.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(UNSELECTED_STAR_COLOR, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(SELECTED_STAR_COLOR_DARK, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(SELECTED_STAR_COLOR, PorterDuff.Mode.SRC_ATOP);
    }
}
