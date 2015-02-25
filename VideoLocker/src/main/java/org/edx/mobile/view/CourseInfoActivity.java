package org.edx.mobile.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.base.FindCoursesBaseActivity;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;

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

        // TODO: test the path_id replacement and ensure if curly braces are required in the URL or be removed
        String pathId = getIntent().getStringExtra(EXTRA_PATH_ID);
        String url = Config.getInstance().getEnrollmentConfig()
                .getCourseInfoUrlTemplate()
                .replace("{" + EXTRA_PATH_ID + "}", pathId);
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.loadUrl(url);
    }
}