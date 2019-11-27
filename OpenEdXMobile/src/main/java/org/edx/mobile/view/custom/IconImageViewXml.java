package org.edx.mobile.view.custom;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.widget.IconImageView;

/**
 * Extends {@link IconImageView} to make it displayable in XML,
 * by registering the necessary icon font modules on load.
 */
public class IconImageViewXml extends IconImageView {
    static {
        // Ensure that the icon font modules are registered on class load.
        Iconify.with(new FontAwesomeModule());
    }

    public IconImageViewXml(@NonNull Context context) {
        super(context);
    }

    public IconImageViewXml(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IconImageViewXml(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
