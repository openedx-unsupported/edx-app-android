package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.user.FormField;

public class FormFieldTextAreaActivity extends BaseSingleFragmentActivity {

    public static final String EXTRA_FIELD = "field";
    public static final String EXTRA_VALUE = "value";

    public static Intent newIntent(@NonNull Context context, @NonNull FormField field, @Nullable String currentValue) {
        return new Intent(context, FormFieldTextAreaActivity.class)
                .putExtra(EXTRA_FIELD, field)
                .putExtra(EXTRA_VALUE, currentValue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();
    }

    @Override
    public Fragment getFirstFragment() {
        final FormFieldTextAreaFragment fragment = new FormFieldTextAreaFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
