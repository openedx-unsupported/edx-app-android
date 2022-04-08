package org.edx.mobile.profiles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.NewUserProfileFragment;

public class UserProfileActivity extends BaseSingleFragmentActivity {
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_USERTYPE = "usertype";

    @Inject
    private Config config;

    public static Intent newIntent(@NonNull Context context, @NonNull String username,@NonNull String userType) {
        return new Intent(context, UserProfileActivity.class)
                .putExtra(EXTRA_USERNAME, username)
                .putExtra(EXTRA_USERTYPE, userType);
    }
    public static Intent newIntent(@NonNull Context context, @NonNull String username) {
        return new Intent(context, UserProfileActivity.class)
                .putExtra(EXTRA_USERNAME, username);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // hideToolbarShadow();
        hideToolbar();
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.PROFILE_VIEW);
    }

    @Override
    public Fragment getFirstFragment() {
     //   return UserProfileFragment.newInstance(getIntent().getStringExtra(EXTRA_USERNAME));
        return NewUserProfileFragment.newInstance(getIntent().getStringExtra(EXTRA_USERNAME),getIntent().getStringExtra(EXTRA_USERTYPE));
    }
}
