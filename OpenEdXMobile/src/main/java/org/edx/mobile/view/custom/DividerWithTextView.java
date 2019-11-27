package org.edx.mobile.view.custom;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;


import org.edx.mobile.R;

public class DividerWithTextView extends RelativeLayout {

    private TextView tv;

    public DividerWithTextView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DividerWithTextView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setText(CharSequence text)    {
        tv.setText(text);
    }

    private void init(@NonNull Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.view_divider_with_text, this);
        tv = (TextView)findViewById(R.id.divider_with_text_tv);
        tv.setText(context.obtainStyledAttributes(attrs, new int[]{android.R.attr.text}).getText(0));
    }
}
