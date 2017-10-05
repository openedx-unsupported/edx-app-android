package org.edx.mobile.interfaces;

/**
 * Interface to provide callbacks for a {@link android.webkit.WebView}.
 */
public interface WebViewStatusListener {
    /**
     * Clear contents/HTML of the WebView.
     */
    void clearWebView();

    /**
     * Show progress wheel while loading the web page.
     */
    void showLoadingProgress();

    /**
     * Hide progress wheel after the web page completes loading.
     */
    void hideLoadingProgress();
}
