package org.edx.mobile.view.launch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.ActivityLaunchBinding;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.view.PresenterActivity;

public class LaunchActivity extends PresenterActivity<LaunchPresenter, LaunchPresenter.LaunchViewInterface> {

    private ActivityLaunchBinding activityLaunchBinding;

    public static Intent newIntent(Context context) {
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        if (context instanceof Activity)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        else
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launchIntent;
    }

    @NonNull
    @Override
    protected LaunchPresenter createPresenter(@Nullable Bundle savedInstanceState) {
        return new LaunchPresenter(environment.getConfig());
    }

    @NonNull
    @Override
    protected LaunchPresenter.LaunchViewInterface createView(@Nullable Bundle savedInstanceState) {
        //Register for Login Receiver before checking for logged in user
        enableLoginCallback();

        //We need to stop the Launch Activity from launching if the user has logged in
        PrefManager pm = new PrefManager(LaunchActivity.this, PrefManager.Pref.LOGIN);
        if (pm.getCurrentUserProfile() != null) {
            finish();
        }

        activityLaunchBinding = DataBindingUtil.setContentView(this, R.layout.activity_launch);

        activityLaunchBinding.launchSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showLogin(LaunchActivity.this);
            }
        });

        activityLaunchBinding.launchSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    environment.getSegment().trackUserSignUpForAccount();
                } catch (Exception e) {
                    logger.error(e);
                }
                environment.getRouter().showRegistration(LaunchActivity.this);
            }
        });

        environment.getSegment().trackScreenView(ISegment.Screens.LAUNCH_ACTIVITY);

        return new LaunchPresenter.LaunchViewInterface(){
            @Override
            public void setCourseDiscoveryButton(boolean enabled) {
                if(!enabled) {
                    activityLaunchBinding.courseDiscoveryButton.setVisibility(View.GONE);
                }
            }
        };
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


    @Override
    protected boolean createOptionsMenu(Menu menu) {
        // Launch screen doesn't have any menu
        return true;
    }
}
