package org.edx.mobile.tta.ui.connect;

import android.webkit.JavascriptInterface;

public class JavaScriptInterfaceConnectNavigation {
    ConnectDashboardActivity connectactivity;

    public JavaScriptInterfaceConnectNavigation(ConnectDashboardActivity activity) {
        connectactivity = activity;
    }

    @JavascriptInterface
    public void addReplyOnComment(String parent_id) {
        connectactivity.addReplyToComment(Integer.parseInt(parent_id));
    }
}
