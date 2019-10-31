package org.humana.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.humana.mobile.base.BaseSingleFragmentActivity;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.user.DataType;
import org.humana.mobile.user.FormField;

import roboguice.inject.InjectExtra;

import static org.humana.mobile.user.DataType.COUNTRY;
import static org.humana.mobile.user.DataType.LANGUAGE;

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
