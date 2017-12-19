package org.edx.mobile.view.custom;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

public class IndicatorController {
    private static final int FIRST_PAGE_NUM = 0;

    private Context context;
    private LinearLayout dotLayout;
    private List<ImageView> dots;
    private int slideCount;

    public View newInstance(@NonNull Context context) {
        this.context = context;
        dotLayout = (LinearLayout) View.inflate(context, R.layout.default_indicator, null);
        return dotLayout;
    }

    public void initialize(int slideCount) {
        dots = new ArrayList<>();
        this.slideCount = slideCount;

        for (int i = 0; i < slideCount; i++) {
            ImageView dot = new ImageView(context);
            dot.setImageDrawable(UiUtil.getDrawable(context, R.drawable.indicator_dot_active));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            dotLayout.addView(dot, params);
            dots.add(dot);
        }

        selectPosition(FIRST_PAGE_NUM);
    }

    public void selectPosition(int index) {
        for (int i = 0; i < slideCount; i++) {
            int drawableId = i == index ? (R.drawable.indicator_dot_active) : (R.drawable.indicator_dot_inactive);
            Drawable drawable = UiUtil.getDrawable(context, drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }
}
