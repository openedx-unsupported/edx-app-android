package org.edx.mobile.view.custom;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class ETextView extends TextView {

    private final Logger logger = new Logger(getClass().getName());

    public ETextView(Context context) {
        super(context);
    }

    public ETextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttrs(context, attrs);
    }

    public ETextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttrs(context, attrs);
    }

    private void processAttrs(Context context, AttributeSet attrs) {
        if(isInEditMode())
            return;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.custom_view, 0, 0);

        try {
            // check for the font attribute and setup font
            String fontFileName = a.getString(R.styleable.custom_view_font);
            /*if(fontFileName==null){
                fontFileName = attrs.getAttributeValue(null, "font");
            }*/
            Typeface font = FontFactory.getInstance().getFont(context,fontFileName);
            setTypeface(font);
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
        //  a.recycle();
        }
    }
}
