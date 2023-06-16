package org.edx.mobile.module.registration.view;

import android.text.InputType;
import android.view.Gravity;
import android.view.View;

import org.edx.mobile.module.registration.model.RegistrationFormField;

class RegistrationTextAreaView extends RegistrationEditTextView {

    // Number of lines for TextArea
    private static final int INIT_LINES = 1;
    private static final int MAX_LINES = 7;

    public RegistrationTextAreaView(RegistrationFormField field, View view) {
        super(field, view);

        mEditText.setLines(INIT_LINES);
        mEditText.setMaxLines(MAX_LINES);

        // allow multiline text
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        // text should start from the left-top
        mEditText.setGravity(Gravity.START | Gravity.TOP);
    }
}
