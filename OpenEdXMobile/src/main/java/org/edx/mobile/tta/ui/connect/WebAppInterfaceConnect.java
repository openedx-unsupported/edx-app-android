package org.edx.mobile.tta.ui.connect;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class WebAppInterfaceConnect {
    Activity connectactivity;
    WebView mxWebview;

    public WebAppInterfaceConnect(Activity activity, WebView mWebview) {
        connectactivity = activity;
        mxWebview=mWebview;
    }

    @JavascriptInterface
    public void goBack() {

        connectactivity.runOnUiThread(() -> {
            if(mxWebview.canGoBack())
            {
                mxWebview.goBack();
            }
            else
            {
                connectactivity.finish();
            }
        });
    }
}
