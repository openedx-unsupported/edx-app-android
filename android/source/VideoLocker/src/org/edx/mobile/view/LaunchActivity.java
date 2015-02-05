package org.edx.mobile.view;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.view.custom.EButton;
import org.edx.mobile.view.custom.ETextView;

public class LaunchActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        overridePendingTransition(R.anim.slide_in_from_right,
                R.anim.slide_out_to_left);

        ETextView sign_in_tv = (ETextView) findViewById(R.id.sign_in_tv);
        sign_in_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().showLogin(LaunchActivity.this);
            }
        });

        EButton sign_up_button = (EButton) findViewById(R.id.sign_up_btn);
        sign_up_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().showRegistration(LaunchActivity.this);
            }
        });

        try{
            segIO.screenViewsTracking(ISegment.Values.LAUNCH_ACTIVITY);
        }catch(Exception e){
            logger.error(e);
        }
    }
}
