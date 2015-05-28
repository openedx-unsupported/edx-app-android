package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.apache.http.HttpStatus;
import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.view.common.PageViewStateCallback;
import org.edx.mobile.view.common.TaskProcessCallback;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CourseUnitWebviewFragment extends Fragment implements PageViewStateCallback {
    HtmlBlockModel unit;
    TaskProcessCallback callback;
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

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unit = getArguments() == null ? null :
            (HtmlBlockModel) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_unit_webview, container, false);
        //TODO - populate view here
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //should we recover here?
        WebView webView = (WebView)getView().findViewById(R.id.course_unit_webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                if ( callback != null )
                    callback.finishProcess();
                if ( errorCode == HttpStatus.SC_FORBIDDEN || errorCode == HttpStatus.SC_UNAUTHORIZED || errorCode == HttpStatus.SC_NOT_FOUND){
                    tryToLoadWebview(view);
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
            public void onPageFinished(WebView view, String url) {
                if ( callback != null )
                    callback.finishProcess();

                String sessionIdRefreshUrl = Api.getSessionTokenExchangeUrl();
                if ( url.equals(sessionIdRefreshUrl) ){
                    String cookies = CookieManager.getInstance().getCookie(url);
                    String[] cookieValues=cookies.split(";");
                    for (int i = 0; i < cookieValues.length; i++) {
                        String[] split = cookieValues[i].split("=");
                        if ( split[0].trim().equals(PrefManager.Key.SESSION_ID) ){
                            PrefManager pref = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                            pref.put(PrefManager.Key.AUTH_ASSESSMENT_SESSION_ID, split[1]);
                            //we can not get the expiration date from cookie, so just set it to expire for one day
                            pref.put(PrefManager.Key.AUTH_ASSESSMENT_SESSION_EXPIRATION, new Date().getTime() + 3600 * 24 * 1000 );
                            break;
                        }
                    }
                    tryToLoadWebview(view);
                    return;
                }
                view.loadUrl("javascript:EdxAssessmentView.resize(document.body.getBoundingClientRect().height)");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
               return false;
            }
        });
        webView.addJavascriptInterface(this, "EdxAssessmentView");

        tryToLoadWebview(webView);
    }

    private void tryToLoadWebview(WebView webView){
        if ( callback != null )
            callback.startProcess();
        if ( unit != null) {
            if ( unit.isGraded() || unit.isGradedSubDAG() ){
                getView().findViewById(R.id.webview_header_text).setVisibility(View.VISIBLE);
            } else {
                getView().findViewById(R.id.webview_header_text).setVisibility(View.GONE);
            }

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

            Long lastRefresh = pref.getLong(PrefManager.Key.AUTH_ASSESSMENT_SESSION_ID_REFRESH_TIME);
            Long sessionIdExpirationDate = pref.getLong(PrefManager.Key.AUTH_ASSESSMENT_SESSION_EXPIRATION);
            String sessionId = pref.getString(PrefManager.Key.AUTH_ASSESSMENT_SESSION_ID);
            Long curTime = new Date().getTime();

            //if refresh sesssionid happens within last min, we dont try to refresh to avoid infinite loop
            //and just go ahead to load the pag
            if ( lastRefresh + 60000 > curTime) {
                map.put("Cookie", PrefManager.Key.SESSION_ID + "=" + sessionId );
                webView.loadUrl(unit.getBlockUrl(), map);
            } else {
                //if session id is expired or session id is not available, we try to refresh session id
                if ( sessionIdExpirationDate < curTime || TextUtils.isEmpty(sessionId) ){
                    pref.put(PrefManager.Key.AUTH_ASSESSMENT_SESSION_ID_REFRESH_TIME, curTime);
                    webView.loadUrl(Api.getSessionTokenExchangeUrl(), map);
                } else {
                    map.put("Cookie", PrefManager.Key.SESSION_ID + "=" + sessionId );
                    webView.loadUrl(unit.getBlockUrl(), map);
                }
            }
        }
    }

    @JavascriptInterface
    public void resize(final float height) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView webView = (WebView)getView().findViewById(R.id.course_unit_webView);
                webView.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) (height * getResources().getDisplayMetrics().density)));
            }
        });
    }
    /// for PageViewStateCallback ///
    @Override
    public void onPageShow() {

    }

    @Override
    public void onPageDisappear() {

    }
}
