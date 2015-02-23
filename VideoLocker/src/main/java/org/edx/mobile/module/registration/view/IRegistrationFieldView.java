package org.edx.mobile.module.registration.view;

import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.JsonElement;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.registration.model.RegistrationAgreement;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationFormField;

public interface IRegistrationFieldView {
    // Returns the value that should be sent to the server when registering.
    // Can be null to indicate do not send the field
    JsonElement getCurrentValue();
    boolean hasValue();
    RegistrationFormField getField();
    View getView();
    void handleError(String errorMessage);
    boolean isValidInput();
    void setEnabled(boolean enabled);
    void setActionListener(IActionListener actionListener);

    public static interface IActionListener {
        void onClickAgreement(RegistrationAgreement agreement);
    }

    /**
     * Factory class to get instance {@link IRegistrationFieldView}
     * for the given {@link org.edx.mobile.module.registration.model.RegistrationFormField}.
     */
    public static class Factory {

        private static final Logger logger = new Logger(IRegistrationFieldView.Factory.class);

        public static IRegistrationFieldView getInstance(LayoutInflater inflater, RegistrationFormField field) {
            RegistrationFieldType fieldType = field.getFieldType();

            if (fieldType.equals(RegistrationFieldType.EMAIL)) {
                View view = inflater.inflate(R.layout.view_register_edit_text, null);
                return new RegistrationEmailView(field, view);
            }
            else if (fieldType.equals(RegistrationFieldType.PASSWORD)) {
                View view = inflater.inflate(R.layout.view_register_edit_text, null);
                return new RegistrationPasswordView(field, view);
            }
            else if (fieldType.equals(RegistrationFieldType.TEXT)) {
                View view = inflater.inflate(R.layout.view_register_edit_text, null);
                return new RegistrationTextView(field, view);
            }
            else if (fieldType.equals(RegistrationFieldType.TEXTAREA)) {
                View view = inflater.inflate(R.layout.view_register_edit_text, null);
                return new RegistrationTextAreaView(field, view);
            }
            else if (fieldType.equals(RegistrationFieldType.MULTI)) {
                View view = inflater.inflate(R.layout.view_register_spinner, null);
                return new RegistrationSelectView(field, view);
            }
            else if (fieldType.equals(RegistrationFieldType.CHECKBOX)) {
                if (field.getAgreement() != null) {
                    View view = inflater.inflate(R.layout.view_register_agreement, null);
                    return new RegistrationAgreementView(field, view);
                }
                else {
                    View view = inflater.inflate(R.layout.view_register_checkbox, null);
                    return new RegistrationCheckBoxView(field, view);
                }
            }
            else {
                logger.warn(String.format("unknown field type %s found in RegistrationDescription, skipping it",
                        fieldType.toString()));
                return null;
            }
        }
    }
}
