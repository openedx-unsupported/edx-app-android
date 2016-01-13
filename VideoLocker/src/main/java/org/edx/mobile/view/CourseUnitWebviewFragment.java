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
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.event.SessionIdRefreshEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.services.ViewPagerDownloadManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

/**
 *
 */
public class CourseUnitWebviewFragment extends CourseUnitFragment{

    protected final Logger logger = new Logger(getClass().getName());

    private final static String EMPTY_HTML = "<html><body></body></html>";

    private boolean pageIsLoaded;

    @InjectView(R.id.loading_indicator)
    private ProgressBar progressWheel;

    @InjectView(R.id.course_unit_webView)
    private WebView webView;

    @InjectView(R.id.content_unavailable_error_text)
    private TextView errorTextView;

    /**
     * Create a new instance of fragment
     */
    static CourseUnitWebviewFragment newInstance(HtmlBlockModel unit) {
        CourseUnitWebviewFragment f = new CourseUnitWebviewFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroyView();
    }

    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(getContext())) {
            hideErrorMessage();
        } else {
            showErrorMessage(R.string.reset_no_network_message, FontAwesomeIcons.fa_wifi);
        }
    }

    public void onEvent(SessionIdRefreshEvent event) {
        if (event.success) {
            tryToLoadWebView(false);
        } else {
            hideLoadingProgress();
        }
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
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
                        ViewPagerDownloadManager.instance.done(CourseUnitWebviewFragment.this, false);
                        showErrorMessage(R.string.network_error_message,
                                FontAwesomeIcons.fa_exclamation_circle);
                    }

                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                                    WebResourceResponse errorResponse) {
                        didReceiveError = true;
                        switch (errorResponse.getStatusCode()) {
                            case HttpURLConnection.HTTP_FORBIDDEN:
                            case HttpURLConnection.HTTP_UNAUTHORIZED:
                            case HttpURLConnection.HTTP_NOT_FOUND:
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
                        ViewPagerDownloadManager.instance.done(CourseUnitWebviewFragment.this, true);
                        hideLoadingProgress();
                    }
                };
        client.setAllLinksAsExternal(true);
        //webView.addJavascriptInterface(this, "EdxAssessmentView");

        if(  ViewPagerDownloadManager.USING_UI_PRELOADING ) {
            if (ViewPagerDownloadManager.instance.inInitialPhase(unit)) {
                ViewPagerDownloadManager.instance.addTask(this);
            } else {
                tryToLoadWebView(true);
            }
        }
    }

    /**
     * Shows the error message with the given icon, if the web page failed to load
     * @param errorMsg The error message to show
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
                            .colorRes(context, R.color.edx_grayscale_neutral_light),
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
            PrefManager pref = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
            AuthResponse auth = pref.getCurrentAuth();
            Map<String, String> map = new HashMap<String, String>();
            if (auth == null || !auth.isSuccess()) {
                // this might be a login with Facebook or Google
                String token = pref.getString(PrefManager.Key.AUTH_TOKEN_SOCIAL);
                if (token != null) {
                    map.put("Authorization", token);
                }
            } else {
                map.put("Authorization", String.format("%s %s", auth.token_type, auth.access_token));
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
    public void run(){
        if ( this.isRemoving() || this.isDetached()){
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

    public void onResume() {
        super.onResume();
        if ( hasComponentCallback != null ){
            CourseComponent component = hasComponentCallback.getComponent();
            if (component != null && component.equals(unit)){
                try {
                    tryToLoadWebView(false);
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }
    }

    //the problem with viewpager is that it loads this fragment
    //and calls onResume even it is not visible.
    //which breaks the normal behavior of activity/fragment
    //lifecycle.
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if ( ViewPagerDownloadManager.USING_UI_PRELOADING )
            return;
        if ( ViewPagerDownloadManager.instance.inInitialPhase(unit) )
            return;
        if (isVisibleToUser) {
            tryToLoadWebView(false);
        }else{
            tryToClearWebView();
        }
    }

    @JavascriptInterface
    public void resize(final float height) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) (height * getResources().getDisplayMetrics().density)));
            }
        });
    }


    private void showLoadingProgress(){
        progressWheel.setVisibility(View.VISIBLE);
    }

    private void hideLoadingProgress(){
        progressWheel.setVisibility(View.GONE);
    }
}
