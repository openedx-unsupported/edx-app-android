package org.edx.mobile.module.registration.view;

import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.JsonElement;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationFormField;

public interface IRegistrationFieldView {
    // Returns the value that should be sent to the server when registering.
    // Can be null to indicate do not send the field
    JsonElement getCurrentValue();
    boolean hasValue();
    RegistrationFormField getField();
    View getView();
    void setInstructions(@Nullable String instructions);
    void handleError(String errorMessage);
    boolean isValidInput();
    void setEnabled(boolean enabled);
    void setActionListener(IActionListener actionListener);

    /**
     * Get the specific child view which should be focused when the error child view is visible.
     *
     * @return Child view which needs to be focused in case of error.
     */
    View getOnErrorFocusView();

    /**
     * used to programmatically set the value
     * return false if not implemented yet, or can not set the value
     *
     */
    boolean setRawValue(String value);

    interface IActionListener {
        void onClickAgreement();
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
                View view = inflater.inflate(R.layout.view_register_auto_complete, null);
                return new RegistrationSelectView(field, view);
            }
            else if (fieldType.equals(RegistrationFieldType.PLAINTEXT)) {
                // For now we aren't using this field type
                return null;
            } else {
                logger.error(new Exception(
                        String.format("Unknown field type found for field named: %s in RegistrationDescription, skipping it!",
                                field.getName())
                ));
                return null;
            }
        }
    }

}
