package org.edx.mobile.view;

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

    private Subscription logInSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Finish this activity if the user logs in from another activity
        logInSubscription = loginAPI.getLogInEvents().subscribe(new Observer<LogInEvent>() {
            @Override
            public void onData(@NonNull LogInEvent data) {
                finish();
            }

            @Override
            public void onError(@NonNull Throwable error) {
                // This will never happen
                throw new RuntimeException(error);
            }
        });
        final ActivityDiscoveryLaunchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_discovery_launch);
        binding.logIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showLogin(DiscoveryLaunchActivity.this);
            }
        });
        binding.signUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getSegment().trackUserSignUpForAccount();
                environment.getRouter().showRegistration(DiscoveryLaunchActivity.this);
            }
        });
        binding.discoverCourses.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showFindCourses(DiscoveryLaunchActivity.this);
            }
        });
        binding.exploreSubjects.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME: Where should this go?
                environment.getRouter().showFindCourses(DiscoveryLaunchActivity.this);
            }
        });
        environment.getSegment().trackScreenView(ISegment.Screens.LAUNCH_ACTIVITY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logInSubscription.unsubscribe();
    }

    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false; // Disable menu inherited from BaseFragmentActivity
    }
}
