package org.edx.mobile.view.registration;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.registration.RegistrationFormField;
import org.edx.mobile.model.registration.RegistrationOption;

public class RegistrationSpinnerView implements IRegistrationFieldView {

    protected static final Logger logger = new Logger(RegistrationEditTextView.class);
    private RegistrationFormField mField;
    private View mView;
    protected Spinner mInputView;
    private TextView mErrorView, mInstructionView;

    public RegistrationSpinnerView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (Spinner) view.findViewById(R.id.input_spinner);
        this.mErrorView = (TextView) view.findViewById(R.id.input_spinner_error);
        this.mInstructionView = (TextView) view.findViewById(R.id.input_spinner_instruction);

        // set hint
        mInputView.setPrompt(mField.getLabel());

        // display default value and set the entries
        ArrayAdapter<RegistrationOption> adapter = new ArrayAdapter<>(mInputView.getContext(), android.R.layout.simple_spinner_item);
        int i=0, selectedIndex=0;
        for (RegistrationOption option : mField.getOptions()) {
            if (option.isDefaultValue()) {
                selectedIndex = i;
            }
            adapter.add(option);
            i++;
        }
        mInputView.setAdapter(adapter);
        mInputView.setSelection(selectedIndex);

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
        return new JsonPrimitive(mField.getOptions().get(mInputView.getSelectedItemPosition()).getValue());
    }

    @Override
    public boolean hasValue() {
        return !mField.getOptions().get(mInputView.getSelectedItemPosition()).getValue().isEmpty();
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
            handleError(mField.getErrorMessage().getRequired());
            return false;
        }

        // check if length restrictions are followed
        int inputLength = getCurrentValue().getAsString().length();
        if (inputLength < mField.getRestriction().getMinLength()) {
            handleError(mField.getErrorMessage().getMinLength());
            return false;
        }
        if (mField.getRestriction().getMaxLength() > 0
                && inputLength > mField.getRestriction().getMaxLength()) {
            handleError(mField.getErrorMessage().getMaxLength());
            return false;
        }

        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mInputView.setEnabled(enabled);
    }
}
