package org.edx.mobile.view.custom;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.event.SessionIdRefreshEvent;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.WebViewUtil;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

import static org.edx.mobile.util.WebViewUtil.EMPTY_HTML;

/**
 * A custom webview which authenticates the user before loading a page,
 * Javascript can also be passed in arguments for evaluation.
 */
public class AuthenticatedWebView extends FrameLayout {
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    private LoginPrefs loginPrefs;

    @InjectView(R.id.loading_indicator)
    private ProgressBar progressWheel;

    @InjectView(R.id.webview)
    protected WebView webView;

    @InjectView(R.id.error_text)
    private TextView errorTextView;

    private URLInterceptorWebViewClient webViewClient;
    private String url;
    private String javascript;
    private boolean pageIsLoaded;
    private boolean didReceiveError;

    public AuthenticatedWebView(Context context) {
        super(context);
        init();
    }

    public AuthenticatedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AuthenticatedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.authenticated_webview, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public URLInterceptorWebViewClient getWebViewClient() {
        return webViewClient;
    }

    /**
     * Initialize the webview (must call it before loading some url).
     *
     * @param fragmentActivity   Reference of fragment activity.
     * @param isAllLinksExternal A flag to treat every link as external link and open in external
     *                           web browser.
     */
    @SuppressLint("SetJavaScriptEnabled")
    public void initWebView(@NonNull FragmentActivity fragmentActivity, boolean isAllLinksExternal) {
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webViewClient = new URLInterceptorWebViewClient(fragmentActivity, webView) {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                didReceiveError = true;
                hideLoadingProgress();
                pageIsLoaded = false;
                showErrorMessage(R.string.network_error_message, FontAwesomeIcons.fa_exclamation_circle);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            WebResourceResponse errorResponse) {
                // If error occurred for web page request
                if (request.getUrl().toString().equals(view.getUrl())) {
                    didReceiveError = true;
                    switch (errorResponse.getStatusCode()) {
                        case HttpStatus.FORBIDDEN:
                        case HttpStatus.UNAUTHORIZED:
                        case HttpStatus.NOT_FOUND:
                            EdxCookieManager.getSharedInstance(getContext())
                                    .tryToRefreshSessionCookie();
                            break;
                        default:
                            hideLoadingProgress();
                            break;
                    }
                    pageIsLoaded = false;
                    showErrorMessage(R.string.network_error_message, FontAwesomeIcons.fa_exclamation_circle);
                }
                super.onReceivedHttpError(view, request, errorResponse);
            }

            public void onPageFinished(WebView view, String url) {
                if (!NetworkUtil.isConnected(getContext())) {
                    showErrorView(getResources().getString(R.string.reset_no_network_message),
                            FontAwesomeIcons.fa_wifi);
                    hideLoadingProgress();
                    pageIsLoaded = false;
                    return;
                }
                if (didReceiveError) {
                    didReceiveError = false;
                    return;
                }
                if (url != null && url.equals("data:text/html," + EMPTY_HTML)) {
                    //we load a local empty html page to release the memory
                } else {
                    pageIsLoaded = true;
                    hideErrorMessage();
                }

                if (pageIsLoaded && !TextUtils.isEmpty(javascript)) {
                    evaluateJavascript();
                } else {
                    hideLoadingProgress();
                }
                super.onPageFinished(view, url);
            }
        };

        webViewClient.setAllLinksAsExternal(isAllLinksExternal);
    }

    public void loadUrl(boolean forceLoad, @NonNull String url) {
        loadUrlWithJavascript(forceLoad, url, null);
    }

    public void loadUrlWithJavascript(boolean forceLoad, @NonNull final String url, @Nullable final String javascript) {
        this.url = url;
        this.javascript = javascript;
        if (!TextUtils.isEmpty(javascript)) {
            webView.addJavascriptInterface(new JsInterface(), "JsInterface");
        }
        tryToLoadWebView(forceLoad);
    }

    private void evaluateJavascript() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(javascript, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    hideLoadingProgress();
                }
            });
        } else {
            webView.loadUrl("javascript:" + javascript);
            // Javascript evaluation takes some time, so hide progressbar after 1 sec
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideLoadingProgress();
                }
            }, 1000);
        }
    }

    private void tryToLoadWebView(boolean forceLoad) {
        System.gc(); //there is a well known Webview Memory Issue With Galaxy S3 With 4.3 Update

        if ((!forceLoad && pageIsLoaded) || progressWheel == null) {
            return;
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (!NetworkUtil.isConnected(getContext())) {
            showErrorMessage(R.string.reset_no_network_message, FontAwesomeIcons.fa_wifi);
            return;
        }

        showLoadingProgress();

        if (!TextUtils.isEmpty(url)) {
            // Requery the session cookie if unavailable or expired.
            final EdxCookieManager cookieManager = EdxCookieManager.getSharedInstance(getContext());
            if (cookieManager.isSessionCookieMissingOrExpired()) {
                cookieManager.tryToRefreshSessionCookie();
            } else {
                didReceiveError = false;
                webView.loadUrl(url);
            }
        }
    }

    public void tryToClearWebView() {
        pageIsLoaded = false;
        WebViewUtil.clearWebviewHtml(webView);
    }

    private void showLoadingProgress() {
        if (!TextUtils.isEmpty(javascript)) {
            // Hide webview to disable a11y during loading page, disabling a11y is not working in this case
            webView.setVisibility(View.GONE);
        }
        progressWheel.setVisibility(View.VISIBLE);
    }

    private void hideLoadingProgress() {
        progressWheel.setVisibility(View.GONE);
        if (didReceiveError) {
            webView.setVisibility(View.GONE);
        } else {
            webView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows the error message with the given icon, if the web page failed to load
     *
     * @param errorMsg  The error message to show
     * @param errorIcon The error icon to show with the error message
     */
    private void showErrorMessage(@StringRes int errorMsg, @NonNull Icon errorIcon) {
        if (!pageIsLoaded) {
            tryToClearWebView();
            showErrorView(getResources().getString(errorMsg), errorIcon);
        }
    }

    private void showErrorView(@NonNull String errorMsg, @NonNull Icon errorIcon) {
        final Context context = getContext();
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(errorMsg);
        errorTextView.setCompoundDrawablesWithIntrinsicBounds(null,
                new IconDrawable(context, errorIcon)
                        .sizeRes(context, R.dimen.content_unavailable_error_icon_size)
                        .colorRes(context, R.color.edx_brand_gray_back),
                null, null
        );
    }

    /**
     * Hides the error message view and reloads the web page if it wasn't already loaded
     */
    private void hideErrorMessage() {
        errorTextView.setVisibility(View.GONE);
        if (!pageIsLoaded || didReceiveError) {
            tryToLoadWebView(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(getContext())) {
            hideErrorMessage();
        } else {
            showErrorMessage(R.string.reset_no_network_message, FontAwesomeIcons.fa_wifi);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SessionIdRefreshEvent event) {
        if (event.success) {
            tryToLoadWebView(false);
        } else {
            hideLoadingProgress();
        }
    }

    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
    }

    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
    }

    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
    }

    /**
     * Javascript interface class to define android functions which could be called from javascript.
     */
    private class JsInterface {
        @JavascriptInterface
        public void showErrorMessage(@NonNull final String errorMsg) {
            if (!TextUtils.isEmpty(errorMsg)) {
                webView.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                didReceiveError = true;
                                webView.setVisibility(View.GONE);
                                showErrorView(errorMsg, FontAwesomeIcons.fa_exclamation_circle);
                            }
                        }
                );
            }
        }
    }
}
