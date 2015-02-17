package org.edx.mobile.module.registration.view;

import android.content.Context;
import android.util.AttributeSet;

import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.view.custom.ESpinner;

/**
 * Created by rohan on 2/16/15.
 */
public class RegistrationOptionSpinner extends ESpinner<RegistrationOption> {

    public RegistrationOptionSpinner(Context context) {
        super(context);
    }

    public RegistrationOptionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RegistrationOptionSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
