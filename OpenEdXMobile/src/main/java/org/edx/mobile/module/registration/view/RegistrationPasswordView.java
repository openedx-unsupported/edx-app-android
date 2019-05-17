package org.edx.mobile.module.registration.view;

import android.text.InputType;
import android.view.View;

import org.edx.mobile.module.registration.model.RegistrationFormField;

/**
 * Created by rohan on 2/11/15.
 */
class RegistrationPasswordView extends RegistrationEditTextView {

    public RegistrationPasswordView(RegistrationFormField field, View view) {
        super(field, view);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

}
