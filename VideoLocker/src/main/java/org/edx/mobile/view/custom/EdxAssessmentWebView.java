package org.edx.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by rohan on 3/12/15.
 */
public class EdxAssessmentWebView extends WebView {

    public EdxAssessmentWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setLoadsImagesAutomatically(true);

    }

}
