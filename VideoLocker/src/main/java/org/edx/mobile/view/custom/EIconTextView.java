package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import org.edx.mobile.R;

public class EIconTextView extends IconTextViewXml {
    public EIconTextView(Context context) {
        super(context);
    }

    public EIconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttrs(context, attrs);
    }

    public EIconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        processAttrs(context, attrs);
    }

    private void processAttrs(Context context, AttributeSet attrs) {
        if (isInEditMode()) return;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.custom_view, 0, 0);
        // Check for the font attribute and setup font
        String fontFileName = a.getString(R.styleable.custom_view_font);
        a.recycle();
        if (fontFileName == null) {
            // If font is not defined, then set default font
            fontFileName = "OpenSans-Regular.ttf";
        }
        Typeface font = FontFactory.getInstance().getFont(context, fontFileName);
        setTypeface(font);
    }
}
