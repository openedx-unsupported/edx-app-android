package org.edx.mobile.view;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.base.WebViewCourseInfoFragment;
import org.edx.mobile.module.analytics.Analytics;

public class CourseInfoActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_INFO_SCREEN);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    public Fragment getFirstFragment() {
        final WebViewCourseInfoFragment fragment = new WebViewCourseInfoFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
