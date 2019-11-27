package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;

import org.edx.mobile.base.BaseSingleFragmentActivity;

public class ProgramWebViewActivity extends BaseSingleFragmentActivity {
    private static final String ARG_URL = "url";
    private static final String ARG_TITLE = "title";

    public static Intent newIntent(@NonNull Context context, @NonNull String url, @NonNull String title) {
        return new Intent(context, ProgramWebViewActivity.class)
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
