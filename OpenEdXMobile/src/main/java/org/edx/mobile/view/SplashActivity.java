package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;

// We are extending the normal Activity class here so that we can use Theme.NoDisplay, which does not support AppCompat activities
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();

        if (!isTaskRoot()) {
            return; // This stops from opening again from the Splash screen when minimized
        }

        final IEdxEnvironment environment = MainApplication.getEnvironment(this);
        if (environment.getUserPrefs().getProfile() != null) {
            environment.getRouter().showMyCourses(SplashActivity.this);
        } else if (!environment.getConfig().isRegistrationEnabled()){
            startActivity(environment.getRouter().getLogInIntent());
        } else {
            environment.getRouter().showLaunchScreen(SplashActivity.this);
        }
    }
}
