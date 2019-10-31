package org.humana.mobile.module.registration.view;

import android.text.InputType;
import android.view.View;

import org.humana.mobile.R;
import org.humana.mobile.module.registration.model.RegistrationFormField;
import org.humana.mobile.util.InputValidationUtil;

/**
 * Created by rohan on 2/11/15.
 */
class RegistrationEmailView extends RegistrationEditTextView {

    public RegistrationEmailView(RegistrationFormField field, View view) {
        super(field, view);
        mTextInputEditText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    @Override
    public boolean isValidInput() {
        boolean isValidInput = super.isValidInput();
        if(isValidInput){
            if(!InputValidationUtil.isValidEmail(getCurrentValue().getAsString())){
                handleError(getView().getResources().getString(R.string.error_invalid_email));
                isValidInput = false;
            }
        }
        return isValidInput;
    }
}
