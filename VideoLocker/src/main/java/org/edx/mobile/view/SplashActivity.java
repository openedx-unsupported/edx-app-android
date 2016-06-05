package org.edx.mobile.view;

import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseAppActivity;
import org.edx.mobile.core.IEdxEnvironment;

public class SplashActivity extends BaseAppActivity {
    @Inject
    private IEdxEnvironment environment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();

        if (!isTaskRoot()) {
            return; // This stops from opening again from the Splash screen when minimized
        }

        if (environment.getUserPrefs().getProfile() != null) {
            environment.getRouter().showMyCourses(SplashActivity.this);
        } else {
            environment.getRouter().showLaunchScreen(SplashActivity.this);
        }
    }
}
