package org.edx.mobile.view.custom;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

// Just like a ViewPager only it doesn't scroll side to side
// Per http://stackoverflow.com/questions/7814017/is-it-possible-to-disable-scrolling-on-a-viewpager
public class NonScrollingViewPager extends ViewPager {

    public NonScrollingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollingViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }
}
