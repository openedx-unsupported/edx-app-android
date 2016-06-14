package org.edx.mobile.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.LogInEvent;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.databinding.ActivityDiscoveryLaunchBinding;
import org.edx.mobile.databinding.ActivityLaunchBinding;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.observer.Observer;
import org.edx.mobile.util.observer.Subscription;

public class DiscoveryLaunchActivity extends BaseFragmentActivity {

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    LoginAPI loginAPI;

    private final static int LOG_IN_REQUEST_CODE = 42; // Arbitrary, unique request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityDiscoveryLaunchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_discovery_launch);
        binding.logIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(LoginActivity.newIntent(DiscoveryLaunchActivity.this), LOG_IN_REQUEST_CODE);
            }
        });
        binding.signUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getSegment().trackUserSignUpForAccount();
                startActivityForResult(RegisterActivity.newIntent(DiscoveryLaunchActivity.this), LOG_IN_REQUEST_CODE);
            }
        });
        binding.discoverCourses.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showFindCourses(DiscoveryLaunchActivity.this);
            }
        });
        binding.exploreSubjects.setVisibility(View.GONE); // TODO: delete this line once we implement listener
        binding.exploreSubjects.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: Where should this go?
            }
        });
        environment.getSegment().trackScreenView(ISegment.Screens.LAUNCH_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOG_IN_REQUEST_CODE && resultCode == RESULT_OK) {
            // We initiated the log in screen, so let's go to my courses.
            environment.getRouter().showMyCourses(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loginPrefs.getUsername() != null) {
            finish(); // We're logged in now, finish this activity.
        }
    }

    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false; // Disable menu inherited from BaseFragmentActivity
    }
}
