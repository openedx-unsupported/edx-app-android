package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.edx.mobile.R;

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

    private ETextView titleView;

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
        String title = attrArray.getString(styleable.TitleRowView_title);
        float fontSize = attrArray.getDimension(styleable.TitleRowView_titleFontSize, getResources().getDimension(R.dimen.default_row_text));
        DecorationStyle decorationStyle =DecorationStyle.values()[attrArray.getInt(styleable.TitleRowView_decorationStyle, DecorationStyle.CENTER.ordinal())];


        if ( decorationStyle !=  DecorationStyle.LEFT ) {
            View leftDashView = new View(context);
            leftDashView.setBackgroundColor(color.grey_act_background);
            addView(leftDashView, new LinearLayout.LayoutParams(0, 1, 1));
        }

        titleView = new ETextView(context);
        titleView.setText(title);
        titleView.setTextSize(fontSize);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
        params.setMargins(5,0, 5, 0);
        addView(titleView, params);

        if ( decorationStyle != DecorationStyle.RIGHT ) {
            View rightDashView = new View(context);
            rightDashView.setBackgroundColor(color.grey_act_background);
            addView(rightDashView, new LinearLayout.LayoutParams(0, 1, 1));
        }

        attrArray.recycle();
    }

    public void setTitle(String title){
        titleView.setText(title);
    }

}