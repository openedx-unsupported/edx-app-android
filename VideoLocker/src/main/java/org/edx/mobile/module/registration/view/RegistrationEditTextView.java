package org.edx.mobile.module.registration.view;

import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.registration.model.RegistrationFormField;

import java.util.Locale;

class RegistrationEditTextView implements IRegistrationFieldView {

    protected static final Logger logger = new Logger(RegistrationEditTextView.class);
    protected RegistrationFormField mField;
    private View mView;
    protected EditText mInputView;
    private TextView mErrorView, mInstructionView;
    private IActionListener actionListener;

    public RegistrationEditTextView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (EditText) view.findViewById(R.id.txt_input);
        this.mErrorView = (TextView) view.findViewById(R.id.txt_input_error);
        this.mInstructionView = (TextView) view.findViewById(R.id.txt_input_instruction);

        // set max lines for this input to be 1
        mInputView.setLines(1);

        // apply max length
        if (mField.getRestriction().getMaxLength() > 0) {
            // otherwise, you may end up disabling the field

            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(mField.getRestriction().getMaxLength());
            mInputView.setFilters(FilterArray);
        }

        // set hint
        mInputView.setHint(mField.getLabel());

        // display default value
        mInputView.setText(mField.getDefaultValue());

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
        return new JsonPrimitive(mInputView.getText().toString());
    }

    @Override
    public boolean hasValue() {
        return !mInputView.getText().toString().isEmpty();
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
        if (mField.isRequired() && !hasValue()) {
            String errorMessage = mField.getErrorMessage().getRequired();
            if(errorMessage==null || errorMessage.isEmpty()){
                errorMessage = getView().getResources().getString(R.string.error_enter_field,
                        mField.getLabel());
            }
            handleError(errorMessage);
            return false;
        }

        // check if length restrictions are followed
        int inputLength = getCurrentValue().getAsString().length();
        if (inputLength < mField.getRestriction().getMinLength()) {
            String errorMessage = mField.getErrorMessage().getMinLength();
            if(errorMessage==null || errorMessage.isEmpty()){
                errorMessage = getView().getResources().getString(R.string.error_min_length,
                        mField.getLabel(), mField.getRestriction().getMinLength());
            }
            handleError(errorMessage);
            return false;
        }
        if (mField.getRestriction().getMaxLength() > 0
                && inputLength > mField.getRestriction().getMaxLength()) {
            String errorMessage = mField.getErrorMessage().getMaxLength();
            if(errorMessage==null || errorMessage.isEmpty()){
                errorMessage = getView().getResources().getString(R.string.error_max_length,
                        mField.getLabel(), mField.getRestriction().getMaxLength());
            }
            handleError(errorMessage);
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
