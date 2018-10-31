package org.edx.mobile.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.interfaces.WebViewStatusListener;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.util.links.DefaultActionListener;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

public abstract class BaseWebViewFindCoursesActivity extends BaseFragmentActivity
        implements WebViewStatusListener {
    private static final int LOG_IN_REQUEST_CODE = 42;
    private static final String INSTANCE_COURSE_ID = "enrollCourseId";
    private static final String INSTANCE_EMAIL_OPT_IN = "enrollEmailOptIn";

    private EdxWebView webView;
    private ProgressBar progressWheel;
    private String lastClickEnrollCourseId;
    private boolean lastClickEnrollEmailOptIn;

    private FullScreenErrorNotification errorNotification;
    private URLInterceptorWebViewClient client;
    private DefaultActionListener defaultActionListener;

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarAsActionBar();
        webView = (EdxWebView) findViewById(R.id.webview);
        progressWheel = (ProgressBar) findViewById(R.id.loading_indicator);
        errorNotification = new FullScreenErrorNotification(webView);

        webView.getSettings().setDomStorageEnabled(true);

        setupWebView();

        if (null != savedInstanceState) {
            lastClickEnrollCourseId = savedInstanceState.getString(INSTANCE_COURSE_ID);
            lastClickEnrollEmailOptIn = savedInstanceState.getBoolean(INSTANCE_EMAIL_OPT_IN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        webView.destroy();
    }

    private void setupWebView() {
        defaultActionListener = new DefaultActionListener(this, progressWheel,
                new DefaultActionListener.EnrollCallback() {
                    @Override
                    public void onResponse(@NonNull EnrolledCoursesResponse course) {

                    }

                    @Override
                    public void onFailure(@NonNull Throwable error) {
                    }

                    @Override
                    public void onUserNotLoggedIn(@NonNull String courseId, boolean emailOptIn) {
                        lastClickEnrollCourseId = courseId;
                        lastClickEnrollEmailOptIn = emailOptIn;
                        startActivityForResult(environment.getRouter().getRegisterIntent(), LOG_IN_REQUEST_CODE);
                    }
                });
        client = new URLInterceptorWebViewClient(this, webView);
        // if all the links are to be treated as external
        client.setAllLinksAsExternal(isAllLinksExternal());
        client.setActionListener(defaultActionListener);
        client.setPageStatusListener(pageStatusListener);
    }

    /**
     * Loads the given URL into {@link #webView}.
     *
     * @param url The URL to load.
     */
    protected void loadUrl(@NonNull String url) {
        if (client != null) {
            client.setLoadingInitialUrl(true);
        }
        WebViewUtil.loadUrlBasedOnOsVersion(this, webView, url, this, errorNotification, okHttpClientProvider);
    }

    @Override
    public void showLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.VISIBLE);
        }
        if (webView != null) {
            webView.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.GONE);
        }
        if (webView != null) {
            webView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void clearWebView() {
        WebViewUtil.clearWebviewHtml(webView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOG_IN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            defaultActionListener.onClickEnroll(lastClickEnrollCourseId, lastClickEnrollEmailOptIn);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_COURSE_ID, lastClickEnrollCourseId);
        outState.putBoolean(INSTANCE_EMAIL_OPT_IN, lastClickEnrollEmailOptIn);
    }

    /**
     * By default, all links will not be treated as external.
     * Depends on host, as long as the links have same host, they are treated as non-external links.
     *
     * @return
     */
    protected boolean isAllLinksExternal() {
        return false;
    }

    /*
     * In order to avoid reflection issues of public functions in event bus especially those that
     * aren't available on a certain api level, this listener has been refactored to a class
     * variable which is better explained in following references:
     * https://github.com/greenrobot/EventBus/issues/149
     * http://greenrobot.org/eventbus/documentation/faq/
     */
    private URLInterceptorWebViewClient.IPageStatusListener pageStatusListener = new URLInterceptorWebViewClient.IPageStatusListener() {
        @Override
        public void onPageStarted() {
            showLoadingProgress();
        }

        @Override
        public void onPageFinished() {
            hideLoadingProgress();
        }

        @Override
        public void onPageLoadError(WebView view, int errorCode, String description,
                                    String failingUrl) {
            errorNotification.showError(BaseWebViewFindCoursesActivity.this,
                    new HttpStatusException(Response.error(HttpStatus.SERVICE_UNAVAILABLE,
                            ResponseBody.create(MediaType.parse("text/plain"), description))));
            clearWebView();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onPageLoadError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse,
                                    boolean isMainRequestFailure) {
            if (isMainRequestFailure) {
                errorNotification.showError(BaseWebViewFindCoursesActivity.this,
                        new HttpStatusException(Response.error(errorResponse.getStatusCode(),
                                ResponseBody.create(MediaType.parse(errorResponse.getMimeType()),
                                        errorResponse.getReasonPhrase()))));
                clearWebView();
            }
        }

        @Override
        public void onPageLoadProgressChanged(WebView view, int progress) {
        }
    };
}
