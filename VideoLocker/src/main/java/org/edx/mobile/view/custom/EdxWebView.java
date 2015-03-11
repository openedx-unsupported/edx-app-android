package org.edx.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by rohan on 3/11/15.
 */
public class EdxWebView extends WebView {

    public EdxWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadsImagesAutomatically(true);
    }
}
