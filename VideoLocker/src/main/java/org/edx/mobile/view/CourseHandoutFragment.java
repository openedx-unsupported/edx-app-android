package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.GetHandoutTask;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import roboguice.fragment.RoboFragment;

public class CourseHandoutFragment extends RoboFragment {

    public WebView webview;

    static public String TAG = CourseHandoutFragment.class.getCanonicalName();
    static public String ENROLLMENT = TAG + ".enrollment";
    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    ISegment segIO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                .getSerializable(ENROLLMENT);


        try{
            segIO.screenViewsTracking(courseData.getCourse().getName() +
                    " - Handouts");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_handout, container,
                false);

        webview = (WebView) view.findViewById(R.id.webview);
        new URLInterceptorWebViewClient(webview) {

            @Override
            public void onOpenExternalURL(String url) {
                new BrowserUtil().open(getActivity(), url);
            }
        };

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!(NetworkUtil.isConnected(getActivity()))) {
            showHandoutsOffline();
        }

        try {
            final Bundle bundle = getArguments();
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(ENROLLMENT);
            loadData(courseData);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void loadData(EnrolledCoursesResponse enrollment) {
        GetHandoutTask task = new GetHandoutTask(getActivity(), enrollment) {

            @Override
            public void onSuccess(HandoutModel result) {
                try {
                    if(result!=null&&(!TextUtils.isEmpty(result.handouts_html))){
                        hideEmptyHandoutMessage();
                        webview.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), result.handouts_html,
                                "text/html",Encoding.UTF_8.toString(),null);
                        webview.setWebViewClient(new WebViewClient(){
                            @Override
                            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                                new BrowserUtil().open(getActivity(), url);
                                return true;
                            }
                        });
                    }else{
                        showEmptyHandoutMessage();
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                    showEmptyHandoutMessage();
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                showEmptyHandoutMessage();
            }
        };
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.api_spinner);
        task.setProgressDialog(progressBar);
        task.execute();

    }

    private void showEmptyHandoutMessage(){
        try{
            if(webview!=null){
                webview.setVisibility(View.GONE);
            }
            if(getView()!=null){
                getView().findViewById(R.id.no_coursehandout_tv).setVisibility(View.VISIBLE);
            }
        }catch(Exception e){
            logger.error(e);
        }

    }

    private void hideEmptyHandoutMessage(){
        try{
            if(webview!=null){
                webview.setVisibility(View.VISIBLE);
            }
            if(getView()!=null){
                getView().findViewById(R.id.no_coursehandout_tv).setVisibility(View.GONE);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    public void showHandoutsOffline(){
        UiUtil.showMessage(CourseHandoutFragment.this.getView()
                ,getString(R.string.offline_handouts_text));
    }
}
