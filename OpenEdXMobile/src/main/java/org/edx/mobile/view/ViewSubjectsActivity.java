package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.logger.Logger;

public class ViewSubjectsActivity extends BaseSingleFragmentActivity {
    protected Logger logger = new Logger(getClass().getSimpleName());

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, ViewSubjectsActivity.class);
    }

    @Override
    public Fragment getFirstFragment() {
        return new ViewSubjectsFragment();
    }
}
