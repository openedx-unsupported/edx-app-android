package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static org.edx.mobile.R.*;

/**
 * custom view to support 3 type of text decoration for one row
 * <li>---Text---</li>
 * <li>Text------</li>
 * <li>------Text</li>
 * The support custom attributes are
 *   app:title="@string/or_sign_up_with_email"
 *   app:titleFontSize="8sp"
 *   app:decorationStyle="left"  -- decorationStyle is enum type, with values {center,left, right}, default is center
 */
public class TitleRowView extends LinearLayout {

    public static enum DecorationStyle {CENTER, LEFT, RIGHT}

    private TextView titleView;

    public TitleRowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setAttributes(context, attrs);
    }

    public TitleRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setAttributes(context, attrs);
    }

    private void setAttributes(Context context, AttributeSet attrs) {

        if (isInEditMode()){return;}

        TypedArray attrArray = context.obtainStyledAttributes(attrs, styleable.TitleRowView);
        String title = attrArray.getString(styleable.TitleRowView_titleText);
        float fontSize = attrArray.getDimension(styleable.TitleRowView_titleFontSize, 12.0f);
        DecorationStyle decorationStyle =DecorationStyle.values()[attrArray.getInt(styleable.TitleRowView_decorationStyle, DecorationStyle.CENTER.ordinal())];
        int textColor = attrArray.getColor(styleable.TitleRowView_android_textColor, Color.BLACK);
        int leftColor = attrArray.getColor(styleable.TitleRowView_leftColor, ContextCompat.getColor(context, color.neutralLight));
        int rightColor = attrArray.getColor(styleable.TitleRowView_rightColor, ContextCompat.getColor(context, color.neutralLight));

        if ( decorationStyle !=  DecorationStyle.LEFT ) {
            View leftDashView = new View(context);
            leftDashView.setBackgroundColor(leftColor);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 1, 1);
            params.setMargins(0, 0, 10, 0); //substitute parameters for left, top, right, bottom
            addView(leftDashView, params);
        }

        titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(fontSize);
        titleView.setTextColor(textColor);
        addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));


        if ( decorationStyle != DecorationStyle.RIGHT ) {
            View rightDashView = new View(context);
            rightDashView.setBackgroundColor(rightColor);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 1, 1);
            params.setMargins(10, 0, 0, 0); //substitute parameters for left, top, right, bottom
            addView(rightDashView, params);
        }

        attrArray.recycle();
    }

    public void setTitle(String title){
        titleView.setText(title);
    }

}
