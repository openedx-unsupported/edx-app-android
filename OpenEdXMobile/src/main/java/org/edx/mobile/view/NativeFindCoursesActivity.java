package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.view.dialog.NativeFindCoursesFragment;

/**
 * @deprecated As of release v2.13, see {@link MainDashboardActivity}, {@link MainTabsDashboardFragment}
 * and {@link NativeFindCoursesFragment} as an alternate.
 */
@Deprecated
public class NativeFindCoursesActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, NativeFindCoursesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
        if (environment.getLoginPrefs().getUsername() != null) {
            if (!environment.getConfig().isTabsLayoutEnabled()) {
                addDrawer();
            }
        }
    }

    @Override
    public Fragment getFirstFragment() {
        return new NativeFindCoursesFragment();
    }
}
