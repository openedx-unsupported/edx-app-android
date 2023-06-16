package org.edx.mobile.module.registration.view;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.registration.model.RegistrationFieldType;
import org.edx.mobile.module.registration.model.RegistrationFormField;
import org.edx.mobile.module.registration.model.RegistrationOption;

public class RegistrationSelectView implements IRegistrationFieldView {

    protected static final Logger logger = new Logger(RegistrationSelectView.class);
    private RegistrationFormField mField;
    private View mView;
    private RegistrationOptionAutoCompleteTextView mInputView;
    protected TextInputLayout mTextInputLayout;
    private TextView mInstructionsView;
    private TextView mErrorView;
    private boolean hasFocusLost = false;

    public RegistrationSelectView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mTextInputLayout = (TextInputLayout) view.findViewById(R.id.input_wrapper_auto_complete);
        this.mInputView = (RegistrationOptionAutoCompleteTextView) view.findViewById(R.id.input_auto_complete);
        this.mInstructionsView = (TextView) view.findViewById(R.id.input_auto_complete_instructions);
        this.mErrorView = (TextView) view.findViewById(R.id.input_auto_complete_error);

        // Remove JSON defined default value, which is appropriate for web but not for mobile.
        // e.g. server sends "--" as the default value for a select box, but on mobile we want
        // the default value to be the label of select box like Gender, Country etc.
        for (RegistrationOption option : mField.getOptions()) {
            if (option.isDefaultValue()) {
                mField.getOptions().remove(option);
                break;
            }
        }
        // Set Hint using label text
        mTextInputLayout.setHint(mField.getLabel());

        //Set options
        mInputView.setItems(mField.getOptions());

        setInstructions(field.getInstructions());

        // hide error text view
        mErrorView.setVisibility(View.GONE);

        // This tag is necessary for End-to-End tests to work properly
        mInputView.setTag(mField.getName());

        // Do a11y adjustment
        mInputView.setContentDescription(String.format("%s. %s.", mInputView.getSelectedItemName(), mField.getInstructions()));
        ViewCompat.setImportantForAccessibility(mInstructionsView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

        mInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mInputView.hasValue(s.toString().trim())) {
                    mInputView.setSelectedItem(null);
                }
                // Don't show the error until view has lost the focus at least once
                if (hasFocusLost) {
                    isValidInput();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mInputView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mInputView.showDropDown();
                } else {
                    hasFocusLost = true;
                    isValidInput();
                }
            }
        });
    }

    @Override
    public JsonElement getCurrentValue() {
        // turn text view content into a JsonElement and return it
        return new JsonPrimitive(mInputView.getSelectedItemValue());
    }

    public boolean setRawValue(String value) {
        if (mInputView.hasValue(value)) {
            mInputView.select(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasValue() {
        return (mInputView.getSelectedItem() != null
                && !TextUtils.isEmpty(mInputView.getSelectedItemValue()));
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

            final String errorTag = mInputView.getResources().getString(R.string.label_error);
            mInputView.setContentDescription(String.format("%s. %s. %s, %s.",
                    mInputView.getSelectedItemName(), mField.getInstructions(), errorTag, error));
        }
        else {
            logger.warn("error message not provided, so not informing the user about this error");
        }
    }

    @Override
    public boolean isValidInput() {
        // hide error as we are re-validating the input
        mErrorView.setVisibility(View.GONE);

        mInputView.setContentDescription(String.format("%s. %s.", mInputView.getSelectedItemName(), mField.getInstructions()));

        // check if this is required field and has an input value or field is optional and have some value
        if ((mField.isRequired() || (!mField.isRequired() && mField.getFieldType().equals(RegistrationFieldType.MULTI)
                && mInputView.length() > 0)) && !hasValue()) {
            initializeErrorMessage();
            return false;
        }

        //For select we should not have length checks as there is no input
        return true;
    }

    private void initializeErrorMessage(){
        String errorMessage = mField.getErrorMessage().getRequired();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = getView().getResources().getString(R.string.error_select_or_enter_field,
                    mField.getLabel());
        }
        handleError(errorMessage);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mInputView.setEnabled(enabled);
    }

    @Override
    public void setActionListener(IActionListener actionListener) {
        // no actions for this field
    }

    @Override
    public View getOnErrorFocusView() {
        return mInputView;
    }
}
