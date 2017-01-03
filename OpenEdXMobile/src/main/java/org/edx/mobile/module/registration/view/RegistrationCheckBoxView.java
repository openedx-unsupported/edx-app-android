package org.edx.mobile.module.registration.view;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.registration.model.RegistrationFormField;

class RegistrationCheckBoxView implements IRegistrationFieldView {

    protected static final Logger logger = new Logger(RegistrationCheckBoxView.class);
    private View mView;
    private RegistrationFormField mField;
    private CheckBox mInputView;
    private TextView mInstructionsView;
    private TextView mErrorView;

    public RegistrationCheckBoxView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (CheckBox) view.findViewById(R.id.checkbox_input);
        this.mInstructionsView = (TextView)view.findViewById(R.id.checkbox_input_instructions);
        this.mErrorView = (TextView) view.findViewById(R.id.checkbox_input_error);

        // set hint
        mInputView.setHint(mField.getLabel());

        setInstructions(field.getInstructions());

        // display default value
        mInputView.setChecked(Boolean.getBoolean(mField.getDefaultValue()));

        // hide error text view
        mErrorView.setVisibility(View.GONE);

        // This tag is necessary for End-to-End tests to work properly
        mInputView.setTag(mField.getName());
    }

    @Override
    public JsonElement getCurrentValue() {
        // turn text view content into a JsonElement and return it
        return new JsonPrimitive(mInputView.isChecked());
    }

    public boolean setRawValue(String value){
        try{
            boolean boolValue = Boolean.valueOf(value);
            mInputView.setChecked(boolValue);
            return true;
        }catch(Exception ex){
            return false;
        }
    }

    @Override
    public boolean hasValue() {
        // being checkbox, this always has a value
        return true;
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
    public void setInstructions(@Nullable String instructions) {
        if (instructions != null && !instructions.isEmpty()) {
            mInstructionsView.setVisibility(View.VISIBLE);
            mInstructionsView.setText(instructions);
        }
        else {
            mInstructionsView.setVisibility(View.GONE);
        }
    }

    @Override
    public void handleError(String error) {
        if (error != null && !error.isEmpty()) {
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setText(error);
        }
        else {
            logger.warn("error message not provided, so not informing the user about this error");
        }
    }

    @Override
    public boolean isValidInput() {
        // hide error as we are re-validating the input
        mErrorView.setVisibility(View.GONE);

        // check if this is required field and has an input value
        if (mField.isRequired() && !mInputView.isChecked()) {
            handleError(mField.getErrorMessage().getRequired());
            return false;
        }

        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mInputView.setEnabled(enabled);
    }

    @Override
    public void setActionListener(IActionListener actionListener) {
        // no actions for this field
    }
}
