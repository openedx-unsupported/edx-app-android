package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;

public class UserProfileActivity extends BaseSingleFragmentActivity {
    public static final String EXTRA_USERNAME = "username";

    public static Intent newIntent(@NonNull Context context, @NonNull String username) {
        return new Intent(context, UserProfileActivity.class)
                .putExtra(EXTRA_USERNAME, username);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra(EXTRA_USERNAME));
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        final UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
