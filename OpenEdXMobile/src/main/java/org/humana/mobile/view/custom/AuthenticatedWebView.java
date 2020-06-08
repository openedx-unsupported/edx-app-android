package org.humana.mobile.view.custom;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.humana.mobile.R;
import org.humana.mobile.event.CourseDashboardRefreshEvent;
import org.humana.mobile.event.MainDashboardRefreshEvent;
import org.humana.mobile.event.NetworkConnectivityChangeEvent;
import org.humana.mobile.event.SessionIdRefreshEvent;
import org.humana.mobile.http.HttpStatus;
import org.humana.mobile.http.notifications.FullScreenErrorNotification;
import org.humana.mobile.interfaces.RefreshListener;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.services.EdxCookieManager;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.util.NetworkUtil;
import org.humana.mobile.util.WebViewUtil;

import java.sql.Date;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

import static org.humana.mobile.util.WebViewUtil.EMPTY_HTML;

/**
 * A custom webview which authenticates the user before loading a page,
 * Javascript can also be passed in arguments for evaluation.
 */
public class AuthenticatedWebView extends FrameLayout implements RefreshListener {
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    private LoginPrefs loginPrefs;

    @InjectView(R.id.loading_indicator)
    private ProgressBar progressWheel;

    @InjectView(R.id.webview)
    protected WebView webView;

    private FullScreenErrorNotification fullScreenErrorNotification;
    private URLInterceptorWebViewClient webViewClient;
    private String url;
    private String javascript;
    private boolean pageIsLoaded;
    private boolean didReceiveError;
    private boolean isManuallyReloadable;

    private OnResponseCallback<String> htmlCallback;
    private boolean isInitiated = false;

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private FragmentActivity fragmentActivity;

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
        fullScreenErrorNotification = new FullScreenErrorNotification(webView);
    }

    public URLInterceptorWebViewClient getWebViewClient() {
        return webViewClient;
    }

    public WebView getWebView() {
        return webView;
    }

    /**
     * Initialize the webview (must call it before loading some url).
     *
     * @param fragmentActivity     Reference of fragment activity.
     * @param isAllLinksExternal   A flag to treat every link as external link and open in external
     *                             web browser.
     * @param isManuallyReloadable A flag that decides if we should give showLoading/hideLoading reload button
     *                             with full screen error.
     */
    @SuppressLint("SetJavaScriptEnabled")
    public void initWebView(@NonNull FragmentActivity fragmentActivity, boolean isAllLinksExternal,
                            boolean isManuallyReloadable) {
        isInitiated = true;
        this.fragmentActivity = fragmentActivity;
        this.isManuallyReloadable = isManuallyReloadable;
//        webView.clearCache(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        long a,b;

        webView.setWebChromeClient(new WebChromeClient(){
            // openFileChooser for Android 3.0+
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("video");
                fragmentActivity.startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try
                {
                    fragmentActivity.startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video");
                fragmentActivity.startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("video");


                // video/mp4
                //video/x-msvideo
                //video/x-ms-wmv
                fragmentActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }
        });


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

    public void setHtmlCallback(OnResponseCallback<String> htmlCallback) {
        this.htmlCallback = htmlCallback;
    }

    public boolean isInitiated() {
        return isInitiated;
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
                    if (htmlCallback != null){
                        htmlCallback.onSuccess(value);
                    }
                    hideLoadingProgress();
                }
            });
        } else {
            webView.loadUrl("javascript:" + javascript);
            // Javascript evaluation takes some time, so hideLoading progressbar after 1 sec
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
     * @param errorMsg  The error message to showLoading
     * @param errorIcon The error icon to showLoading with the error message
     */
    private void showErrorMessage(@StringRes int errorMsg, @NonNull Icon errorIcon) {
        if (!pageIsLoaded) {
            tryToClearWebView();
            showErrorView(getResources().getString(errorMsg), errorIcon);
        }
    }

    private void showErrorView(@NonNull String errorMsg, @NonNull Icon errorIcon) {
        if (isManuallyReloadable) {
            fullScreenErrorNotification.showError(errorMsg, errorIcon, R.string.lbl_reload, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRefresh();
                }
            });
        } else {
            fullScreenErrorNotification.showError(errorMsg, errorIcon, 0, null);
        }
    }

    /**
     * Hides the error message view and reloads the web page if it wasn't already loaded
     */
    private void hideErrorMessage() {
        fullScreenErrorNotification.hideError();
        if (!pageIsLoaded || didReceiveError) {
            tryToLoadWebView(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(getContext())) {
            // If manual reloading is enabled, we don't want the error to disappear and screen to load automatically
            if (!isManuallyReloadable) {
                onRefresh();
            }
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

    @SuppressWarnings("unused")
    public void onEventMainThread(CourseDashboardRefreshEvent event) {
        onRefresh();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(MainDashboardRefreshEvent event) {
        onRefresh();
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

    @Override
    public void onRefresh() {
        pageIsLoaded = false;
        hideErrorMessage();
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

    public boolean isShowingError() {
        return fullScreenErrorNotification != null && fullScreenErrorNotification.isShowing();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != fragmentActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(fragmentActivity.getApplicationContext(), "Failed to Upload IMAGE", Toast.LENGTH_LONG).show();
    }
}
