package org.edx.mobile.view;

import android.os.Bundle;
import android.webkit.WebView;

import org.edx.mobile.R;
import org.edx.mobile.base.FindCoursesBaseActivity;
import org.edx.mobile.util.Config;

public class FindCoursesActivity extends FindCoursesBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_find_courses);
        super.onCreate(savedInstanceState);

        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
        configureDrawer();

        try{
            segIO.screenViewsTracking(getString(R.string.find_courses_title));
        }catch(Exception e){
            logger.error(e);
        }

        loadCourseSearchUrl();
    }

    @Override
    protected void onOnline() {
        super.onOnline();
        loadCourseSearchUrl();
    }

    private void loadCourseSearchUrl() {
        String url = Config.getInstance().getEnrollmentConfig().getCourseSearchUrl();
        WebView webview = (WebView) findViewById(R.id.webview);
        webview.loadUrl(url);
    }
}
