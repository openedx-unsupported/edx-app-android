package org.edx.mobile.view.registration;

import android.text.InputType;
import android.view.View;

import org.edx.mobile.model.registration.RegistrationFormField;

/**
 * Created by rohan on 2/11/15.
 */
public class RegistrationPasswordView extends RegistrationEditTextView {

    public RegistrationPasswordView(RegistrationFormField field, View view) {
        super(field, view);
        mInputView.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }
}
