package org.edx.mobile.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.interfaces.WebViewStatusListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

public abstract class BaseWebViewDiscoverFragment extends OfflineSupportBaseFragment
        implements URLInterceptorWebViewClient.IActionListener, WebViewStatusListener, RefreshListener {
    protected final Logger logger = new Logger(getClass().getName());

    private EdxWebView webView;
    private ProgressBar progressWheel;

    protected FullScreenErrorNotification errorNotification;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    private CourseService courseService;

    @Inject
    private CourseAPI courseApi;

    @Inject
    private OkHttpClientProvider okHttpClientProvider;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = (EdxWebView) view.findViewById(R.id.webview);
        progressWheel = (ProgressBar) view.findViewById(R.id.loading_indicator);

        initWebView();
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
    public void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }

    private void initWebView() {
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(getActivity(), webView);

        // if all the links are to be treated as external
        client.setAllLinksAsExternal(isAllLinksExternal());

        client.setActionListener(this);
        client.setPageStatusListener(pageStatusListener);
    }

    /**
     * Loads the given URL into {@link #webView}.
     *
     * @param url The URL to load.
     */
    protected void loadUrl(@NonNull String url) {
        WebViewUtil.loadUrlBasedOnOsVersion(getContext(), webView, url, this, errorNotification,
                okHttpClientProvider, R.string.lbl_reload, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRefresh();
                    }
                });
    }

    @Override
    public void showLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoadingProgress() {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.GONE);
        }
    }

    @Override
    public void clearWebView() {
        WebViewUtil.clearWebviewHtml(webView);
    }

    @Override
    public void onClickCourseInfo(String pathId) {
        //If Path id is not null or empty then call CourseInfoActivity
        if (!TextUtils.isEmpty(pathId)) {
            logger.debug("PathId" + pathId);
            environment.getRouter().showCourseInfo(getActivity(), pathId);
        }
    }

    @Override
    public void onClickEnroll(String courseId, boolean emailOptIn) {
        //TODO: Implement this when we will stop using CourseInfoActivity & BaseWebViewFindCoursesActivity
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

    /**
     * See description of: {@link org.edx.mobile.view.custom.URLInterceptorWebViewClient.IPageStatusListener#onPageLoadProgressChanged(WebView, int)
     * IPageStatusListener#onPageLoadProgressChanged}.
     */
    protected void onWebViewLoadProgressChanged(int progress) {
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
            errorNotification.showError(getContext(),
                    new HttpStatusException(Response.error(HttpStatus.SERVICE_UNAVAILABLE,
                            ResponseBody.create(MediaType.parse("text/plain"), description))),
                    R.string.lbl_reload, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onRefresh();
                        }
                    });
            clearWebView();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onPageLoadError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse,
                                    boolean isMainRequestFailure) {
            if (isMainRequestFailure) {
                errorNotification.showError(getContext(),
                        new HttpStatusException(Response.error(errorResponse.getStatusCode(),
                                ResponseBody.create(MediaType.parse(errorResponse.getMimeType()),
                                        errorResponse.getReasonPhrase()))),
                        R.string.lbl_reload, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onRefresh();
                            }
                        });
                clearWebView();
            }
        }

        @Override
        public void onPageLoadProgressChanged(WebView view, int progress) {
            onWebViewLoadProgressChanged(progress);
        }
    };
}
