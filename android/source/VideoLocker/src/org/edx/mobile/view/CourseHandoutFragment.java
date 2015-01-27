package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.task.GetHandoutTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.NetworkUtil;


public class CourseHandoutFragment extends CourseDetailBaseFragment {
    public WebView webview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_handout, container,
                false);

        if (!(NetworkUtil.isConnected(getActivity()))) {
            AppConstants.offline_flag = true;
        }else{
            AppConstants.offline_flag = false;
        }

        webview = (WebView) view.findViewById(R.id.webview);

//        webview.getSettings().setPluginsEnabled(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        //This has been commented after Lou's comments of hiding the Zoom Controls
        //webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                BrowserUtil.open(getActivity(), url);
                return true; //the webview will not load the URL
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            final Bundle bundle = getArguments();
            EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable("enrollment");
            loadData(courseData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadData(EnrolledCoursesResponse enrollment) {
        GetHandoutTask task = new GetHandoutTask(getActivity()) {

            @Override
            public void onFinish(HandoutModel result) {
                try {
                    if(result!=null&&(!result.handouts_html.equalsIgnoreCase(""))){
                        hideEmptyHandoutMessage();
                        webview.loadDataWithBaseURL(new Api(context).getBaseUrl(), result.handouts_html,
                                "text/html",Encoding.UTF_8.toString(),null);
                    }else{
                        showEmptyHandoutMessage();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showEmptyHandoutMessage();
                }
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
                showEmptyHandoutMessage();
            }
        };
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.api_spinner);
        task.setProgressDialog(progressBar);
        task.execute(enrollment);
        try{
            segIO.screenViewsTracking(enrollment.getCourse().getName()+
                    " - Handouts");
        }catch(Exception e){
            e.printStackTrace();
        }
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
