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
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.GetCourseInfoTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.NetworkUtil;

public class CourseInfoFragment extends CourseDetailBaseFragment {

    public WebView webview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (!(NetworkUtil.isConnected(getActivity()))) {
            AppConstants.offline_flag = true;
        }else{
            AppConstants.offline_flag = false;
        }

        View view = inflater.inflate(R.layout.fragment_course_info, container,
                false);

        webview = (WebView) view.findViewById(R.id.webview);

//        webview.getSettings().setPluginsEnabled(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setBlockNetworkImage(false);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);
        // webview.getSettings().setUseWideViewPort(true);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                BrowserUtil.open(getActivity(), url);
                return true; // the webview will not load the URL
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
                    .getSerializable(BaseFragmentActivity.EXTRA_ENROLLMENT);
            if(courseData!=null){
                loadData(courseData);
            }else{
                showEmptyInfoMessage();
            }

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void loadData(EnrolledCoursesResponse enrollment) {
        GetCourseInfoTask task = new GetCourseInfoTask(getActivity()) {

            @Override
            public void onFinish(CourseInfoModel result) {
                try {
                    if(result!=null && (!result.overview.equalsIgnoreCase(""))){
                        hideEmptyInfoMessage();
                        webview.loadDataWithBaseURL(new Api(context).getBaseUrl(), result.overview, "text/html",
                                Encoding.UTF_8.toString(), null);
                    }else{
                        showEmptyInfoMessage(); 
                    }
                } catch (Exception ex) {
                    showEmptyInfoMessage();
                    logger.error(ex);
                }
            }
            @Override
            public void onException(Exception ex) {
                showEmptyInfoMessage();
            }
        };
        ProgressBar progressBar = (ProgressBar) getView().findViewById(
                R.id.api_spinner);
        task.setProgressDialog(progressBar);
        task.execute(enrollment);
        try{
            segIO.screenViewsTracking(enrollment.getCourse().getName()+" - Course Info");
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void showEmptyInfoMessage(){
        try{
            if(getView()!=null){
                getView().findViewById(R.id.no_courseinfo_tv).setVisibility(View.VISIBLE);
            }
        }catch(Exception e){
            logger.error(e);
        }

    }

    private void hideEmptyInfoMessage(){
        try{
            if(getView()!=null){
                getView().findViewById(R.id.no_courseinfo_tv).setVisibility(View.GONE);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        /*if(segIO!=null){
            segIO.analyticsFlush();
        }*/
    }
}
