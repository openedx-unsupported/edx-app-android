package org.edx.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.widget.IconTextView;

/**
 * Extends {@link IconTextView} to make it displayable in XML,
 * by registering the necessary icon font modules on load.
 */
public class IconTextViewXml extends IconTextView {
    static {
        // Ensure that the icon font modules are registered on class load.
        Iconify.with(new FontAwesomeModule());
    }

    public IconTextViewXml(Context context) {
        super(context);
    }

    public IconTextViewXml(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconTextViewXml(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
