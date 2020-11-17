package org.edx.mobile.view.custom;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatRatingBar;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;

import org.edx.mobile.R;
import org.edx.mobile.util.ResourceUtil;

import java.util.HashMap;
import java.util.Map;

public class EdxRatingBar extends AppCompatRatingBar {
    @ColorInt
    private final int UNSELECTED_STAR_COLOR = ContextCompat.getColor(getContext(), R.color.neutralDark);
    @ColorInt
    private final int SELECTED_STAR_COLOR = ContextCompat.getColor(getContext(), R.color.primaryBaseColor);
    @ColorInt
    private final int SELECTED_STAR_COLOR_DARK = ContextCompat.getColor(getContext(), R.color.primaryBaseColor);

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
        // Set accessibility label
        setContentDescription(getResources().getString(R.string.rating_bar));
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SELECTED) {
            final Map<String, CharSequence> map = new HashMap<>();
            map.put("rating", String.valueOf((int) getRating()));
            map.put("num_of_stars", String.valueOf(getNumStars()));
            event.setContentDescription(ResourceUtil.getFormattedString(getResources(),
                    R.string.rating_bar_selection, map));
        }
    }
}
