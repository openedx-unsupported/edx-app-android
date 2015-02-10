package org.edx.mobile.view.registration;

import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.R;
import org.edx.mobile.model.registration.RegistrationFormField;

public class RegistrationEditTextView implements IRegistrationFieldView {
    private RegistrationFormField mField;
    private View mView;
    private EditText mInputView;
    private TextView mErrorView, mInstructionView;

    public RegistrationEditTextView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (EditText) view.findViewById(R.id.txt_input);
        this.mErrorView = (TextView) view.findViewById(R.id.txt_input_error);
        this.mInstructionView = (TextView) view.findViewById(R.id.txt_input_instruction);

        // apply max length
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(mField.getRestriction().getMaxLength());
        mInputView.setFilters(FilterArray);

        // set hint
        mInputView.setHint(mField.getPlaceholder());

        // display default value
        mInputView.setText(mField.getDefaultValue());
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
    }

    @Override
    public void validateInput() {
    }
}
