package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.base.BaseSingleFragmentActivity;

import roboguice.inject.InjectExtra;

public class EditUserProfileActivity extends BaseSingleFragmentActivity {
    public static final String EXTRA_USERNAME = "username";

    public static Intent newIntent(@NonNull Context context, @NonNull String username) {
        return new Intent(context, EditUserProfileActivity.class)
                .putExtra(EXTRA_USERNAME, username);
    }

    @Inject
    private EditUserProfileFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
