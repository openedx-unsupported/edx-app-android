package org.edx.mobile.module.registration.view;

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
    private RegistrationFormField mField;
    private View mView;
    protected CheckBox mInputView;
    private TextView mErrorView, mInstructionView;

    public RegistrationCheckBoxView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (CheckBox) view.findViewById(R.id.checkbox_input);
        this.mErrorView = (TextView) view.findViewById(R.id.checkbox_input_error);
        this.mInstructionView = (TextView) view.findViewById(R.id.checkbox_input_instruction);

        // set hint
        mInputView.setHint(mField.getLabel());

        // display default value
        mInputView.setChecked(Boolean.getBoolean(mField.getDefaultValue()));

        // display instructions if available
        if (mField.getInstructions() != null && !mField.getInstructions().isEmpty()) {
            mInstructionView.setVisibility(View.VISIBLE);
            mInstructionView.setText(mField.getInstructions());
        } else {
            mInstructionView.setVisibility(View.GONE);
        }

        // hide error text view
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public JsonElement getCurrentValue() {
        // turn text view content into a JsonElement and return it
        return new JsonPrimitive(mInputView.isChecked());
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
