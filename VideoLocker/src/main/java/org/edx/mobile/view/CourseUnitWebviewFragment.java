package org.edx.mobile.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.apache.http.HttpStatus;
import org.edx.mobile.R;
import org.edx.mobile.event.SessionIdRefreshEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.services.ViewPagerDownloadManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 *
 */
public class CourseUnitWebviewFragment extends CourseUnitFragment{

    protected final Logger logger = new Logger(getClass().getName());

    private final static String EMPTY_HTML = "<html><body></body></html>";

    ProgressBar progressWheel;
    boolean pageIsLoaded;
    WebView webView;
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
        EventBus.getDefault().register(this);
    }

    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEvent(SessionIdRefreshEvent event){
        if ( event.success ){
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
        View v = inflater.inflate(R.layout.fragment_course_unit_webview, container, false);
        progressWheel = (ProgressBar)v.findViewById(R.id.progress_spinner);
        webView = (WebView)v.findViewById(R.id.course_unit_webView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //should we recover here?

        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                hideLoadingProgress();
                pageIsLoaded = false;
                ViewPagerDownloadManager.instance.done(CourseUnitWebviewFragment.this, false);
                if ( errorCode == HttpStatus.SC_FORBIDDEN || errorCode == HttpStatus.SC_UNAUTHORIZED || errorCode == HttpStatus.SC_NOT_FOUND){
                    EdxCookieManager.getSharedInstance().tryToRefreshSessionCookie();
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
            public void onPageFinished(WebView view, String url) {
                if ( url != null && url.equals("data:text/html," + EMPTY_HTML ) ){
                    //we load a local empty html page to release the memory
                } else {
                    pageIsLoaded = true;
                }

                //TODO -disable it for now. as it causes some issues for assessment
                //webview to fit in the screen. But we still need it to show additional
                //compenent below the webview in the future?
               // view.loadUrl("javascript:EdxAssessmentView.resize(document.body.getBoundingClientRect().height)");
                super.onPageFinished(view, url);
                ViewPagerDownloadManager.instance.done(CourseUnitWebviewFragment.this, true);
                hideLoadingProgress();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
               return false;
            }
        });
        //webView.addJavascriptInterface(this, "EdxAssessmentView");

        if(  ViewPagerDownloadManager.USING_UI_PRELOADING ) {
            if (ViewPagerDownloadManager.instance.inInitialPhase(unit))
                ViewPagerDownloadManager.instance.addTask(this);
            else
                tryToLoadWebView(true);
        }
    }




    private void tryToLoadWebView(boolean forceLoad){
        System.gc(); //there is an well known Webview Memory Issues With Galaxy S3 With 4.3 Update

        if ( (!forceLoad && pageIsLoaded) ||  progressWheel == null )
            return;

        showLoadingProgress();

        if ( unit != null) {
            if ( unit.isGraded() ){
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

            Long sessionIdExpirationDate = pref.getLong(PrefManager.Key.AUTH_ASSESSMENT_SESSION_EXPIRATION);
            String sessionId = pref.getString(PrefManager.Key.AUTH_ASSESSMENT_SESSION_ID);
            Long curTime = new Date().getTime();

               //if session id is expired or session id is not available, we try to refresh session id
                if ( sessionIdExpirationDate < curTime || TextUtils.isEmpty(sessionId) ){
                    EdxCookieManager.getSharedInstance().tryToRefreshSessionCookie();
                } else {
                    map.put("Cookie", PrefManager.Key.SESSION_ID + "=" + sessionId );
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


    private void tryToClearWebView(){
        pageIsLoaded = false;
        if ( webView != null)
            webView.loadData(EMPTY_HTML, "text/html", "UTF-8");
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
