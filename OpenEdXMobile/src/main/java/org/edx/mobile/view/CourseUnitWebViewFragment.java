package org.edx.mobile.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
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
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.services.ViewPagerDownloadManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

public class CourseUnitWebViewFragment extends CourseUnitFragment {

    protected final Logger logger = new Logger(getClass().getName());

    private final static String EMPTY_HTML = "<html><body></body></html>";

    private boolean pageIsLoaded;

    @InjectView(R.id.loading_indicator)
    private ProgressBar progressWheel;

    @InjectView(R.id.course_unit_webView)
    private WebView webView;

    @InjectView(R.id.content_unavailable_error_text)
    private TextView errorTextView;

    @Inject
    private LoginPrefs loginPrefs;

    public static CourseUnitWebViewFragment newInstance(HtmlBlockModel unit) {
        CourseUnitWebViewFragment f = new CourseUnitWebViewFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_unit_webview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //should we recover here?

        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        URLInterceptorWebViewClient client =
                new URLInterceptorWebViewClient(getActivity(), webView) {
                    private boolean didReceiveError = false;

                    @Override
                    public void onReceivedError(WebView view, int errorCode,
                                                String description, String failingUrl) {
                        didReceiveError = true;
                        hideLoadingProgress();
                        pageIsLoaded = false;
                        ViewPagerDownloadManager.instance.done(CourseUnitWebViewFragment.this, false);
                        showErrorMessage(R.string.network_error_message,
                                FontAwesomeIcons.fa_exclamation_circle);
                    }

                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                                    WebResourceResponse errorResponse) {
                        didReceiveError = true;
                        switch (errorResponse.getStatusCode()) {
                            case HttpStatus.FORBIDDEN:
                            case HttpStatus.UNAUTHORIZED:
                            case HttpStatus.NOT_FOUND:
                                EdxCookieManager.getSharedInstance().tryToRefreshSessionCookie();
                                break;
                        }
                        showErrorMessage(R.string.network_error_message,
                                FontAwesomeIcons.fa_exclamation_circle);
                    }

                    public void onPageFinished(WebView view, String url) {
                        if (didReceiveError) {
                            didReceiveError = false;
                            return;
                        }
                        if (url != null && url.equals("data:text/html," + EMPTY_HTML)) {
                            //we load a local empty html page to release the memory
                        } else {
                            pageIsLoaded = true;
                        }

                        //TODO -disable it for now. as it causes some issues for assessment
                        //webview to fit in the screen. But we still need it to show additional
                        //compenent below the webview in the future?
                        // view.loadUrl("javascript:EdxAssessmentView.resize(document.body.getBoundingClientRect().height)");
                        ViewPagerDownloadManager.instance.done(CourseUnitWebViewFragment.this, true);
                        hideLoadingProgress();
                    }
                };
        client.setAllLinksAsExternal(true);

        if (ViewPagerDownloadManager.USING_UI_PRELOADING) {
            if (ViewPagerDownloadManager.instance.inInitialPhase(unit)) {
                ViewPagerDownloadManager.instance.addTask(this);
            } else {
                tryToLoadWebView(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
        if (hasComponentCallback != null) {
            CourseComponent component = hasComponentCallback.getComponent();
            if (component != null && component.equals(unit)) {
                try {
                    tryToLoadWebView(false);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.destroy();
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

    /**
     * Shows the error message with the given icon, if the web page failed to load
     *
     * @param errorMsg  The error message to show
     * @param errorIcon The error icon to show with the error message
     */
    private void showErrorMessage(@StringRes int errorMsg, @NonNull Icon errorIcon) {
        if (!pageIsLoaded) {
            tryToClearWebView();
            Context context = getContext();
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(errorMsg);
            errorTextView.setCompoundDrawablesWithIntrinsicBounds(null,
                    new IconDrawable(context, errorIcon)
                            .sizeRes(context, R.dimen.content_unavailable_error_icon_size)
                            .colorRes(context, R.color.edx_brand_gray_back),
                    null, null
            );
        }
    }

    /**
     * Hides the error message view and reloads the web page if it wasn't already loaded
     */
    private void hideErrorMessage() {
        errorTextView.setVisibility(View.GONE);
        if (!pageIsLoaded) {
            tryToLoadWebView(true);
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

        if (unit != null) {
            Map<String, String> map = new HashMap<>();
            final String token = loginPrefs.getAuthorizationHeader();
            if (token != null) {
                map.put("Authorization", token);
            }

            // Requery the session cookie if unavailable or expired if we are on
            // an API level lesser than Marshmallow (which provides HTTP error
            // codes in the error callback for WebViewClient).
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M &&
                    EdxCookieManager.getSharedInstance().isSessionCookieMissingOrExpired()) {
                EdxCookieManager.getSharedInstance().tryToRefreshSessionCookie();
            } else {
                webView.loadUrl(unit.getBlockUrl(), map);
            }
        }
    }

    @Override
    public void run() {
        if (this.isRemoving() || this.isDetached()) {
            ViewPagerDownloadManager.instance.done(this, false);
        } else {
            tryToLoadWebView(true);
        }
    }

    private void tryToClearWebView() {
        pageIsLoaded = false;
        if (webView != null) {
            webView.loadData(EMPTY_HTML, "text/html", "UTF-8");
        }
    }

    //the problem with viewpager is that it loads this fragment
    //and calls onResume even it is not visible.
    //which breaks the normal behavior of activity/fragment
    //lifecycle.
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (ViewPagerDownloadManager.USING_UI_PRELOADING)
            return;
        if (ViewPagerDownloadManager.instance.inInitialPhase(unit))
            return;
        if (isVisibleToUser) {
            tryToLoadWebView(false);
        } else {
            tryToClearWebView();
        }
    }

    private void showLoadingProgress() {
        progressWheel.setVisibility(View.VISIBLE);
    }

    private void hideLoadingProgress() {
        progressWheel.setVisibility(View.GONE);
    }
}
