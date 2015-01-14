package org.edx.mobile.player;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class LandscapePlayerActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landscape_player);

        // this is to lock to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
}
