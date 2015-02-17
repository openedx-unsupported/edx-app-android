package org.edx.mobile.module.registration.view;

import android.text.InputType;
import android.view.Gravity;
import android.view.View;

import org.edx.mobile.module.registration.model.RegistrationFormField;

class RegistrationTextAreaView extends RegistrationEditTextView {

    // Number of lines for TextArea
    private static final int LINES = 5;

    public RegistrationTextAreaView(RegistrationFormField field, View view) {
        super(field, view);

        mInputView.setLines(LINES);
        mInputView.setMaxLines(LINES);

        // allow multiline text
        mInputView.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        // text should start from the left-top
        mInputView.setGravity(Gravity.LEFT | Gravity.TOP);
    }
}