package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;

import javax.inject.Inject;

public class AccountActivity extends BaseSingleFragmentActivity {
    protected Logger logger = new Logger(getClass().getSimpleName());

    @Inject
    private Config config;

    public static Intent newIntent(Context activity) {
        return new Intent(activity, AccountActivity.class);
    }

    @Override
    public Fragment getFirstFragment() {
        return new AccountFragment();
    }
}
