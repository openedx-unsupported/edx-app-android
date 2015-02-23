package org.edx.mobile.view.custom;

import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.edx.mobile.logger.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rohan on 2/2/15.
 *
 * This class represents a custom {@link android.webkit.WebViewClient}.
 * This class is responsible for setting up a given {@link android.webkit.WebView}, assign itself
 * as a {@link android.webkit.WebViewClient} delegate and to intercept URLs being loaded.
 * Depending on the form of URL, this client may forward URL back to the app.
 *
 * This implementation detects host of the first URL being loaded. Further, if any URL intercepted has a different host
 * than the current one, then treats it as an external link and may open in external browser.
 *
 */
public abstract class URLInterceptorWebViewClient extends WebViewClient {

    // URL forms to be intercepted
    private static final String URL_TYPE_ENROLL         = "edxapp://enroll";
    private static final String URL_TYPE_COURSE_INFO    = "edxapp://course_info";
    public static final String PARAM_PATH_ID           = "path_id";
    public static final String COURSE                  = "course/";

    private Logger logger = new Logger(URLInterceptorWebViewClient.class);
    private IActionListener actionListener;
    private IPageStatusListener pageStatusListener;
    private String hostForThisPage = null;

    public URLInterceptorWebViewClient(WebView webView) {
        setupWebView(webView);
    }

    /**
     * Sets action listener for this client. Use this method to get callbacks
     * of actions as declared in {@link org.edx.mobile.view.custom.URLInterceptorWebViewClient.IActionListener}.
     * @param actionListener
     */
    public void setActionListener(IActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Gives page status callbacks like page loading started, finished or error.
     * @param pageStatusListener
     */
    public void setPageStatusListener(IPageStatusListener pageStatusListener) {
        this.pageStatusListener = pageStatusListener;
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

        try {
            // hold on the host of this page, just once
            if (this.hostForThisPage == null) {
                this.hostForThisPage = Uri.parse(url).getHost();
            }
        } catch(Exception ex) {
            logger.error(ex);
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
            pageStatusListener.onPageLoadError();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            if (actionListener == null) {
                logger.warn("you have not set IActionLister to this WebViewClient, " +
                        "you might miss some event");
            }
            logger.debug("loading: " + url);

            if (isCourseInfoLink(url)) {
                // we handled this URL
                return true;
            } else if(isEnrollLink(url)) {
                // we handled this URL
                return true;
            } else if (isExternalLink(url)) {
                // open URL in external web browser
                // return true means the host application handles the url
                // this should open the URL in the browser with user's confirmation
                view.stopLoading();
                // let activity handle this
                onOpenExternalURL(url);
                return false;
            } else {
                // return false means the current WebView handles the url.
                return false;
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
        return super.shouldOverrideUrlLoading(view, url);
    }



    /**
     * Checks if the URL pattern matches with that of COURSE_INFO URL.
     * Extracts path_id from the URL and gives a callback to the registered
     * action listener with path_id parameter.
     * Returns true if pattern matches with COURSE_INFO URL pattern and callback succeeds with
     * extracted parameter, false otherwise.
     * @param strUrl
     * @return
     */
    private boolean isCourseInfoLink(String strUrl) {
        try {
            if (strUrl.startsWith(URL_TYPE_COURSE_INFO)) {
                Uri uri = Uri.parse(strUrl);
                String pathId = uri.getQueryParameter(PARAM_PATH_ID);
                if(pathId.startsWith(URLInterceptorWebViewClient.COURSE)){
                    pathId = pathId.replaceFirst(URLInterceptorWebViewClient.COURSE,"").trim();
                }

                //String pathId = strUrl.replace(URL_TYPE_COURSE_INFO, "").trim();
                if (pathId.isEmpty()) {
                    return false;
                }

                if (actionListener != null) {
                    actionListener.onClickCourseInfo(pathId);
                    logger.debug("found course-info URL: " + strUrl);
                    return true;
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }

        return false;
    }

    /**
     * Returns true if the pattern of the url matches with that of EXTERNAL URL pattern,
     * false otherwise.
     * @param strUrl
     * @return
     */
    private boolean isExternalLink(String strUrl) {
        try {
            Uri uri = Uri.parse(strUrl);
            if (hostForThisPage == null || uri.getHost().equals(hostForThisPage)) {
                // this is not an external link
                return false;
            }
        } catch(Exception ex) {
            logger.error(ex);
        }

        return true;
    }

    /**
     * Checks if the URL pattern matches with that of ENROLL URL.
     * Extracts parameters (course_id and email_opt_in) from the URL and gives a callback to the registered
     * action listener with those parameters.
     * Returns true if pattern matches with ENROLL URL pattern and callback succeeds with
     * extracted parameters, false otherwise.
     * @param strUrl
     * @return
     */
    private boolean isEnrollLink(String strUrl) {
        try {
            if (strUrl.startsWith(URL_TYPE_ENROLL)) {
                Uri uri = Uri.parse(strUrl);

                String query = uri.getQuery().trim();
                String[] params = query.split("&");
                Map<String, String> queryParams = new HashMap<>();
                for (String q : params) {
                    String[] parts = q.split("=");
                    queryParams.put(parts[0], parts[1]);
                }

                String courseId = queryParams.get("course_id");
                boolean emailOptIn = Boolean.parseBoolean(queryParams.get("email_opt_in"));

                if (actionListener != null) {
                    actionListener.onClickEnroll(courseId, emailOptIn);
                    logger.debug("found enroll URL: " + strUrl);
                    return true;
                }
            }
        } catch(Exception ex) {
            logger.error(ex);
        }

        return false;
    }

    /**
     * Callback that gets called when an external URL is being loaded.
     * @param url
     */
    public abstract void onOpenExternalURL(String url);

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
         * @param pathId
         */
        void onClickCourseInfo(String pathId);

        /**
         * Callback that gets called when this client has intercepted Enroll action.
         * Sub-classes or any implementation of this class should override this method to handle
         * enroll action further.
         * @param courseId
         * @param emailOptIn
         */
        void onClickEnroll(String courseId, boolean emailOptIn);
    }

    /**
     * Page state callbacks.
     */
    public static interface IPageStatusListener {
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
        void onPageLoadError();

        /**
         * Callback that indicates that the page is 50 percent loaded.
         */
        void onPagePartiallyLoaded();
    }
}
