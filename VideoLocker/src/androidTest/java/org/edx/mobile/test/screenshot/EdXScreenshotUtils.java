package org.edx.mobile.test.screenshot;

import android.view.View;

import com.facebook.testing.screenshot.ViewHelpers;

/**
 * Created by cleeedx on 4/4/16.
 */
public class EdXScreenshotUtils {

    /**
     * Sets the screenshot size to 480x800dpi.
     * https://developer.android.com/about/dashboards/index.html#Screens
     */
    public static void setDefaultTestResolution(View view) {
        ViewHelpers.setupView(view)
                .setExactWidthDp(480)
                .setExactHeightDp(800)
                .layout();
    }

}
