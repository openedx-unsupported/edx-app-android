package org.edx.mobile.view;

import android.support.annotation.NonNull;

import org.edx.mobile.R;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_find_courses)
public class WebViewExploreSubjectsActivity extends WebViewFindCoursesActivity {
    @NonNull
    @Override
    protected String getInitialUrl() {
        return environment.getConfig().getCourseDiscoveryConfig().getWebViewConfig().getExploreSubjectsUrl();
    }
}
