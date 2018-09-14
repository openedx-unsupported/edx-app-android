package org.edx.mobile.view.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.view.WebViewProgramFragment;

public class AuthenticatedWebViewActivity extends BaseSingleFragmentActivity {
    private static final String ARG_URL = "url";
    private static final String ARG_TITLE = "title";

    public static Intent newIntent(@NonNull Context context, @NonNull String url, @NonNull String title) {
        return new Intent(context, AuthenticatedWebViewActivity.class)
                .putExtra(ARG_URL, url)
                .putExtra(ARG_TITLE, title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String title = getIntent().getStringExtra(ARG_TITLE);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
    }

    @Override
    public Fragment getFirstFragment() {
        return WebViewProgramFragment.newInstance(getIntent().getStringExtra(ARG_URL));
    }
}
