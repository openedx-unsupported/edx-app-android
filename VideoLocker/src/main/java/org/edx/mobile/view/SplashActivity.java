package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;

import org.edx.mobile.core.IEdxEnvironment;

import roboguice.RoboGuice;

// We are extending the normal Activity class here so that we can use Theme.NoDisplay, which does not support AppCompat activities
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();

        if (!isTaskRoot()) {
            return; // This stops from opening again from the Splash screen when minimized
        }

        final IEdxEnvironment environment = RoboGuice.getInjector(getApplicationContext()).getInstance(IEdxEnvironment.class);
        if (environment.getUserPrefs().getProfile() != null) {
            environment.getRouter().showMyCourses(SplashActivity.this);
        } else {
            environment.getRouter().showLaunchScreen(SplashActivity.this);
        }
    }
}
