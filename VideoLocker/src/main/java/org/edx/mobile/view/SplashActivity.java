package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.module.prefs.PrefManager;

public class SplashActivity extends BaseFragmentActivity {

    // Splash screen wait time
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //This stops from opening again from the Splash screen when minimized
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity if it has not been minimized
                if( !isFinishing()) {
                    PrefManager pm =new PrefManager(SplashActivity.this, PrefManager.Pref.LOGIN);

                    Intent intent;
                    if (pm.getCurrentUserProfile() != null) {
                        intent = new Intent(SplashActivity.this, MyCoursesListActivity.class);
                        startActivity(intent);
                    } else {
                        environment.getRouter().showLaunchScreen(SplashActivity.this, false);
                    }
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }
}
