package org.humana.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.inject.Inject;

import org.humana.mobile.R;
import org.humana.mobile.base.BaseFragmentActivity;
import org.humana.mobile.databinding.ActivityLaunchBinding;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.module.prefs.LoginPrefs;

public class LaunchActivity extends BaseFragmentActivity {

    @Inject
    LoginPrefs loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityLaunchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_launch);
        binding.signInTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(environment.getRouter().getLogInIntent());
            }
        });
        binding.signUpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getAnalyticsRegistry().trackUserSignUpForAccount();
                startActivity(environment.getRouter().getRegisterIntent());
            }
        });
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LAUNCH_ACTIVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (environment.getLoginPrefs().getUsername() != null) {
            finish();
            environment.getRouter().showMainDashboard(this);
        }*/
    }
}
