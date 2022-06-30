package org.edx.mobile.view;

import static org.edx.mobile.view.Router.EXTRA_PATH_ID;
import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.module.analytics.Analytics;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DiscoveryActivity extends BaseSingleFragmentActivity {
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, DiscoveryActivity.class);
    }

    public static Intent newIntent(@NonNull Context context, @Nullable String screenName,
                                   @Nullable String pathId) {
        final Intent intent = new Intent(context, DiscoveryActivity.class);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.putExtra(EXTRA_PATH_ID, pathId);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        final Fragment fragment = new MainDiscoveryFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.label_discovery);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthPanelUtils.configureAuthPanel(findViewById(R.id.auth_panel), environment);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }
}
