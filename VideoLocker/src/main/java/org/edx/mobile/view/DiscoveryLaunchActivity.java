package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.databinding.ActivityDiscoveryLaunchBinding;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.LoginPrefs;

public class DiscoveryLaunchActivity extends BaseFragmentActivity {

    @Inject
    LoginPrefs loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityDiscoveryLaunchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_discovery_launch);
        binding.discoverCourses.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getSegment().trackDiscoverCoursesClicked();
                environment.getRouter().showFindCourses(DiscoveryLaunchActivity.this);
            }
        });
        if (environment.getConfig().getCourseDiscoveryConfig().isWebviewCourseDiscoveryEnabled()
                && !TextUtils.isEmpty(environment.getConfig().getCourseDiscoveryConfig().getWebViewConfig().getExploreSubjectsUrl())) {
            binding.exploreSubjects.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    environment.getSegment().trackExploreSubjectsClicked();
                    environment.getRouter().showExploreSubjects(DiscoveryLaunchActivity.this);
                }
            });
        } else {
            // Explore Subjects is only supported for web course discovery
            binding.exploreSubjects.setVisibility(View.INVISIBLE);
        }
        environment.getSegment().trackScreenView(ISegment.Screens.LAUNCH_ACTIVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (environment.getLoginPrefs().getUsername() != null) {
            finish();
            environment.getRouter().showMyCourses(this);
        }
    }

    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false; // Disable menu inherited from BaseFragmentActivity
    }
}
