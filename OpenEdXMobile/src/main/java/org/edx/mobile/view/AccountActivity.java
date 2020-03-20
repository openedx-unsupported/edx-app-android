package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;

import javax.inject.Inject;

import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class AccountActivity extends BaseSingleFragmentActivity {
    protected Logger logger = new Logger(getClass().getSimpleName());

    @Inject
    private Config config;

    public static Intent newIntent(Context activity, @Nullable @ScreenDef String screenName) {
        final Intent intent = new Intent(activity, AccountActivity.class);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        return AccountFragment.newInstance(getIntent().getExtras());
    }
}
