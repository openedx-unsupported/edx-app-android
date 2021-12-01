package org.edx.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import org.edx.mobile.R;

public class DividerWithTextView extends RelativeLayout {

    private TextView tv;
    private View vLeftDivider;
    private View vRightDivider;

    public DividerWithTextView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DividerWithTextView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setText(CharSequence text) {
        tv.setText(text);
    }

    public void setTextColor(@ColorInt int colorId) {
        tv.setTextColor(colorId);
    }

    public void setDividerColor(@ColorInt int colorId) {
        vLeftDivider.setBackgroundColor(colorId);
        vRightDivider.setBackgroundColor(colorId);
    }

    private void init(@NonNull Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.view_divider_with_text, this);
        tv = findViewById(R.id.divider_with_text_tv);
        vLeftDivider = findViewById(R.id.view_left_divider);
        vRightDivider = findViewById(R.id.view_right_divider);

        tv.setText(context.obtainStyledAttributes(attrs, new int[]{android.R.attr.text}).getText(0));
    }
}
