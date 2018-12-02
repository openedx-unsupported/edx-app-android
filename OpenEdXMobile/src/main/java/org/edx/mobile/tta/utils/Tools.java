package org.edx.mobile.tta.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Arjun on 2018/6/20.
 */

public class Tools {
    public static int dp2px(Context mContext, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dpVal, mContext.getResources().getDisplayMetrics());
    }
}
