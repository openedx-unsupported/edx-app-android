package org.edx.mobile.view.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.inject.Inject;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ConfigUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.StandardCharsets;
import org.edx.mobile.util.links.EdxCourseInfoLink;
import org.edx.mobile.util.links.EdxEnrollLink;

import roboguice.RoboGuice;

/**
 * Created by rohan on 2/2/15.
 * <p/>
 * This class represents a custom {@link android.webkit.WebViewClient}.
 * This class is responsible for setting up a given {@link android.webkit.WebView}, assign itself
 * as a {@link android.webkit.WebViewClient} delegate and to intercept URLs being loaded.
 * Depending on the form of URL, this client may forward URL back to the app.
 * <p/>
 * This implementation detects host of the first URL being loaded. Further, if any URL intercepted has a different host
 * than the current one, then treats it as an external link and may open in external browser.
 */
public class URLInterceptorWebViewClient extends WebViewClient {

    private final Logger logger = new Logger(URLInterceptorWebViewClient.class);
    private final FragmentActivity activity;
    private IActionListener actionListener;
    private IPageStatusListener pageStatusListener;
    private String hostForThisPage = null;

    @Inject
    Config config;
    /*
    To help a few views (like Announcements) to treat every link as external link and open in
    external web browser.
     */
    private boolean isAllLinksExternal = false;

    public URLInterceptorWebViewClient(FragmentActivity activity, WebView webView) {
        this.activity = activity;
        RoboGuice.injectMembers(activity, this);
        setupWebView(webView);
    }

    /**
     * Sets action listener for this client. Use this method to get callbacks
     * of actions as declared in {@link org.edx.mobile.view.custom.URLInterceptorWebViewClient.IActionListener}.
     *
     * @param actionListener
     */
    public void setActionListener(IActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Gives page status callbacks like page loading started, finished or error.
     *
     * @param pageStatusListener
     */
    public void setPageStatusListener(IPageStatusListener pageStatusListener) {
        this.pageStatusListener = pageStatusListener;
    }

    /**
     * Sets up the WeView, applies minimal required settings and
     * sets this class itself as WebViewClient.
     *
     * @param webView
     */
    private void setupWebView(WebView webView) {
        webView.setWebViewClient(this);
        //We need to hide the loading progress if the Page starts rendering.
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress > 50) {
                    if (pageStatusListener != null) {
                        pageStatusListener.onPagePartiallyLoaded();
                    }
                }
            }
        });
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        // hold on the host of this page, just once
        if (this.hostForThisPage == null && url != null) {
            this.hostForThisPage = Uri.parse(url).getHost();
        }

        if (pageStatusListener != null) {
            pageStatusListener.onPageStarted();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (pageStatusListener != null) {
            pageStatusListener.onPageFinished();
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (pageStatusListener != null) {
            pageStatusListener.onPageLoadError(view, errorCode, description, failingUrl);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        if (pageStatusListener != null) {
            pageStatusListener.onPageLoadError(view, request, errorResponse,
                    request.getUrl().toString().equals(view.getUrl()));
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (actionListener == null) {
            logger.warn("you have not set IActionLister to this WebViewClient, " +
                    "you might miss some event");
        }
        logger.debug("loading: " + url);
        if (parseCourseInfoLinkAndCallActionListener(url)) {
            // we handled this URL
            return true;
        } else if (parseEnrollLinkAndCallActionListener(url)) {
            // we handled this URL
            return true;
        } else if (isExternalLink(url)) {
            // open URL in external web browser
            // return true means the host application handles the url
            // this should open the URL in the browser with user's confirmation
            BrowserUtil.open(activity, url);
            return true;
        } else {
            // return false means the current WebView handles the url.
            return false;
        }
    }

    public void setAllLinksAsExternal(boolean isAllLinksExternal) {
        this.isAllLinksExternal = isAllLinksExternal;
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Context context = view.getContext().getApplicationContext();

        // suppress external links on ZeroRated network
        if (isExternalLink(url)
                && !ConfigUtil.isWhiteListedURL(url, config)
                && NetworkUtil.isOnZeroRatedNetwork(context, config)
                && NetworkUtil.isConnectedMobile(context)) {
            return new WebResourceResponse("text/html", StandardCharsets.UTF_8.name(), null);
        }
        return super.shouldInterceptRequest(view, url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return shouldInterceptRequest(view, request.getUrl().toString());
    }

    /**
     * Checks if {@param strUrl} is valid course info link and, if so,
     * calls {@link org.edx.mobile.view.custom.URLInterceptorWebViewClient.IActionListener#onClickCourseInfo(String)}
     *
     * @return true if an action listener is set and URL was a valid course info link, false otherwise
     */
    private boolean parseCourseInfoLinkAndCallActionListener(String strUrl) {
        if (null == actionListener) {
            return false;
        }
        final EdxCourseInfoLink link = EdxCourseInfoLink.parse(strUrl);
        if (null == link) {
            return false;
        }
        actionListener.onClickCourseInfo(link.pathId);
        logger.debug("found course-info URL: " + strUrl);
        return true;
    }

    /**
     * Returns true if the pattern of the url matches with that of EXTERNAL URL pattern,
     * false otherwise.
     *
     * @param strUrl
     * @return
     */
    private boolean isExternalLink(String strUrl) {
        return hostForThisPage != null && strUrl != null &&
                !hostForThisPage.equals(Uri.parse(strUrl).getHost());
    }

    /**
     * Checks if {@param strUrl} is valid enroll link and, if so,
     * calls {@link org.edx.mobile.view.custom.URLInterceptorWebViewClient.IActionListener#onClickEnroll(String, boolean)}
     *
     * @return true if an action listener is set and URL was a valid enroll link, false otherwise
     */
    private boolean parseEnrollLinkAndCallActionListener(@Nullable String strUrl) {
        if (null == actionListener) {
            return false;
        }
        final EdxEnrollLink link = EdxEnrollLink.parse(strUrl);
        if (null == link) {
            return false;
        }
        actionListener.onClickEnroll(link.courseId, link.emailOptIn);
        logger.debug("found enroll URL: " + strUrl);
        return true;
    }

    /**
     * Action listener interface for handling enroll link click action
     * and course-info link click action.
     * We may need to add more actions to this interface in future.
     */
    public static interface IActionListener {
        /**
         * Callback that gets called when this client has intercepted Course Info URL.
         * Sub-classes or any implementation of this class should override this method to handle
         * tap of course info URL.
         *
         * @param pathId
         */
        void onClickCourseInfo(String pathId);

        /**
         * Callback that gets called when this client has intercepted Enroll action.
         * Sub-classes or any implementation of this class should override this method to handle
         * enroll action further.
         *
         * @param courseId
         * @param emailOptIn
         */
        void onClickEnroll(String courseId, boolean emailOptIn);
    }

    /**
     * Page state callbacks.
     */
    public interface IPageStatusListener {
        /**
         * Callback that indicates page loading has started.
         */
        void onPageStarted();

        /**
         * Callback that indicates page loading has finished.
         */
        void onPageFinished();

        /**
         * Callback that indicates error during page load.
         */
        void onPageLoadError(WebView view, int errorCode, String description, String failingUrl);

        /**
         * Callback that indicates error during page load.
         */
        void onPageLoadError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse,
                             boolean isMainRequestFailure);

        /**
         * Callback that indicates that the page is 50 percent loaded.
         */
        void onPagePartiallyLoaded();
    }
}
