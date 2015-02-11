package org.edx.mobile.view.registration;

import android.view.View;

import com.google.gson.JsonElement;

import org.edx.mobile.model.registration.RegistrationFormField;

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
}
