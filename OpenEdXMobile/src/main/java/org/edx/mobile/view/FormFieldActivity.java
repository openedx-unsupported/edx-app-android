package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.user.DataType;
import org.edx.mobile.user.FormField;

import roboguice.inject.InjectExtra;

import static org.edx.mobile.user.DataType.COUNTRY;
import static org.edx.mobile.user.DataType.LANGUAGE;

public class FormFieldActivity extends BaseSingleFragmentActivity
        implements Analytics.OnEventListener {

    public static final String EXTRA_FIELD = "field";
    public static final String EXTRA_VALUE = "value";

    @InjectExtra(FormFieldActivity.EXTRA_FIELD)
    private FormField field;

    public static Intent newIntent(@NonNull Context context, @NonNull FormField field, @Nullable String currentValue) {
        return new Intent(context, FormFieldActivity.class)
                .putExtra(EXTRA_FIELD, field)
                .putExtra(EXTRA_VALUE, currentValue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fireScreenEvent();
    }

    @Override
    public Fragment getFirstFragment() {
        final Fragment fragment;
        switch (field.getFieldType()) {
            case SELECT: {
                fragment = new FormFieldSelectFragment();
                break;
            }
            case TEXTAREA: {
                fragment = new FormFieldTextAreaFragment();
                break;
            }
            default: {
                throw new IllegalArgumentException(field.getFieldType().name());
            }
        }
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

    @Override
    public void fireScreenEvent() {
        switch (field.getFieldType()) {
            case SELECT: {
                final String screenName;
                final DataType dataType = field.getDataType();
                if (dataType == COUNTRY) {
                    screenName = Analytics.Screens.PROFILE_CHOOSE_LOCATION;
                } else if (dataType == LANGUAGE) {
                    screenName = Analytics.Screens.PROFILE_CHOOSE_LANGUAGE;
                } else {
                    screenName = Analytics.Screens.PROFILE_CHOOSE_BIRTH_YEAR;
                }
                environment.getAnalyticsRegistry().trackScreenView(screenName);
                break;
            }
            case TEXTAREA: {
                environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.PROFILE_EDIT_TEXT_VALUE);
                break;
            }
        }
    }
}
