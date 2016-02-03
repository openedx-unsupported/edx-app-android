package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.edx.mobile.R;

/**
 * Subclass of ViewPager that disables swipe when
 * it's disabled.
 */
public class DisableableViewPager extends ViewPager {
    public DisableableViewPager(Context context) {
        super(context);
    }

    public DisableableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.DisableableViewPager, 0, 0);
        setEnabled(a.getBoolean(
                R.styleable.DisableableViewPager_android_enabled, true));
        a.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isEnabled() && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isEnabled() && super.onInterceptTouchEvent(event);
    }
}
