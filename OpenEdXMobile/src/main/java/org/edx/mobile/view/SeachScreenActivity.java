package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.deeplink.ScreenDef;

import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class SeachScreenActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(Context activity, @Nullable @ScreenDef String screenName) {
        final Intent intent = new Intent(activity, SeachScreenActivity.class);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideToolbar();
    }
    @Override
    public Fragment getFirstFragment() {
        return SeachScreenFragment.newInstance(getIntent().getExtras());
    }
}
