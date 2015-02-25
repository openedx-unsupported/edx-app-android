package org.edx.mobile.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.http.Api;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.custom.EButton;
import org.edx.mobile.view.custom.ETextView;

public class LaunchActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        overridePendingTransition(R.anim.slide_in_from_right,
                R.anim.slide_out_to_left);

        //The onTick method need not be run in the LaunchActivity
        runOnTick = false;

        ETextView sign_in_tv = (ETextView) findViewById(R.id.sign_in_tv);
        sign_in_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().showLogin(LaunchActivity.this);
            }
        });

        EButton sign_up_button = (EButton) findViewById(R.id.sign_up_btn);
        if (Config.getInstance().isUseDeprecatedRegistrationAPI()) {
            sign_up_button.setVisibility(View.VISIBLE);
            sign_up_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.getInstance().showRegistration(LaunchActivity.this);
                }
            });
        } else {
            // disable registration feature for deprecated API endpoint
            sign_up_button.setVisibility(View.GONE);
        }

        try {
            segIO.screenViewsTracking(ISegment.Values.LAUNCH_ACTIVITY);
        } catch(Exception e) {
            logger.error(e);
        }

        enableLoginCallback();

        fetchRegistrationDescription();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableLoginCallback();
    }

    //Broadcast Receiver to notify all activities to finish if user logs out
    private BroadcastReceiver loginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    protected void enableLoginCallback() {
        // register for login listener
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.USER_LOG_IN);
        registerReceiver(loginReceiver, filter);
    }

    protected void disableLoginCallback() {
        // un-register loginReceiver
        unregisterReceiver(loginReceiver);
    }

    private void fetchRegistrationDescription() {
        Thread th = new Thread() {

            @Override
            public void run() {
                try {
                    Api api = new Api(getApplicationContext());
                    api.downloadRegistrationDescription();
                } catch(Exception ex) {
                    logger.error(ex);
                }
            }
        };
        th.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Launch screen doesn't have any menu
        return true;
    }
}
