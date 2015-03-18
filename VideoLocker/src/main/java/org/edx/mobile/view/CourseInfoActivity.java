package org.edx.mobile.view;

import android.os.Bundle;
import android.webkit.WebView;

import org.edx.mobile.R;
import org.edx.mobile.base.FindCoursesBaseActivity;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

public class CourseInfoActivity extends FindCoursesBaseActivity {

    public static final String EXTRA_PATH_ID = "path_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_find_course_info);
        super.onCreate(savedInstanceState);

        try{
            segIO.screenViewsTracking(ISegment.Values.COURSE_INFO_SCREEN);
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        String pathId = getIntent().getStringExtra(EXTRA_PATH_ID);
        String url = Config.getInstance().getEnrollmentConfig()
                .getCourseInfoUrlTemplate()
                .replace("{" + EXTRA_PATH_ID + "}", pathId);
        WebView webview = (WebView) findViewById(R.id.webview);
        new URLInterceptorWebViewClient(webview) {

            @Override
            public void onOpenExternalURL(String url) {
                BrowserUtil.open(CourseInfoActivity.this, url);
            }
        };

        webview.loadUrl(url);
    }
}