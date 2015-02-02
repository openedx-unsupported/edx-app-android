package org.edx.mobile.view.custom;

import android.app.Activity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.BrowserUtil;

import java.lang.ref.WeakReference;

/**
 * Created by rohan on 2/2/15.
 *
 * This class represents a custom {@link android.webkit.WebViewClient}.
 * This class is responsible for setting up a given {@link android.webkit.WebView}, assign itself
 * as a {@link android.webkit.WebViewClient} delegate and to intercept URLs being loaded.
 * Depending on the form of URL, this client may forward URL back to the app.
 */
public class WebClient extends WebViewClient {

    private Logger logger = new Logger(WebClient.class);
    private WeakReference<Activity> activity;

    public WebClient(Activity activity, WebView webView) {
        this.activity = new WeakReference<Activity>(activity);
        setupWebView(webView);
    }

    /**
     * Sets up the WeView, applies minimal required settings and
     * sets this class itself as WebViewClient.
     * @param webView
     */
    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true);

        webView.setWebChromeClient(new WebChromeClient() {
        });
        webView.setWebViewClient(this);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            if (isCourseInfoLink(url)) {
                onClickCourseInfo(url);
            } else if(isEnrollLink(url)) {
                onClickEnroll(url);
            } else if (isExternalLink(url)) {
                // open URL in external web browser
                BrowserUtil.open(activity.get(), url);
            } else {
                // render URL in the this WebView itself
                view.loadUrl(url);
            }

            return true;
        } catch(Exception ex) {
            logger.error(ex);
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    /**
     * Callback that gets called when this client has intercepted Course Info URL.
     * Sub-classes or any implementation of this class should override this method to handle
     * tap of course info URL.
     * @param url
     */
    public void onClickCourseInfo(String url) {
        // nothing to be done here
    }

    /**
     * Callback that gets called when this client has intercepted Enroll action.
     * Sub-classes or any implementation of this class should override this method to handle
     * enroll action further.
     * @param url
     */
    public void onClickEnroll(String url) {
        // nothing to be done here
    }

    boolean isCourseInfoLink(String url) {
        // TODO: update this method when form of course info URL is determined
        return false;
    }

    boolean isExternalLink(String url) {
        // TODO: update this method when form of external URL is determined
        return false;
    }

    boolean isEnrollLink(String url) {
        // TODO: update this method when form of enroll URL is determined
        return false;
    }
}
