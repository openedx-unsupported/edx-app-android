package org.edx.mobile.view.registration;

import android.view.View;

import com.google.gson.JsonElement;

import org.edx.mobile.model.registration.RegistrationFormField;

public class RegistrationTextAreaView implements IRegistrationFieldView {
    private RegistrationFormField mField;
    private View mView;

    public RegistrationTextAreaView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;
    }

    @Override
    public JsonElement getCurrentValue() {
        // turn text view content into a JsonElement and return it
        return null;
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public RegistrationFormField getField() {
        return mField;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void handleError(String error) {
    }

    @Override
    public void validateInput() {
    }
}
