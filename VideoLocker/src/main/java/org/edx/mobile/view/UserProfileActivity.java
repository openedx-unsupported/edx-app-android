package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;

import roboguice.inject.InjectExtra;

public class UserProfileActivity extends BaseSingleFragmentActivity {
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_SHOW_NAVIGATION_DRAWER = "showNavigationDrawer";

    public static Intent newIntent(@NonNull Context context, @NonNull String username, boolean showNavigationDrawer) {
        return new Intent(context, UserProfileActivity.class)
                .putExtra(EXTRA_USERNAME, username)
                .putExtra(EXTRA_SHOW_NAVIGATION_DRAWER, showNavigationDrawer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra(EXTRA_SHOW_NAVIGATION_DRAWER, false)) {
            configureDrawer();
        } else {
            blockDrawerFromOpening();
        }
    }

    @Override
    public Fragment getFirstFragment() {
        final UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
