package org.edx.mobile.module.registration.view;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.registration.model.RegistrationFormField;
import org.edx.mobile.util.ResourceUtil;

class RegistrationAgreementView implements IRegistrationFieldView {

    private static final Logger logger = new Logger(RegistrationAgreementView.class);
    private RegistrationFormField mField;
    private View mView;
    private TextView mInputView;
    private TextView mInstructionView;
    private TextView mErrorView;
    private IActionListener actionListener;

    public RegistrationAgreementView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (TextView) view.findViewById(R.id.txt_input);
        this.mInstructionView = (TextView)view.findViewById(R.id.txt_input_instruction);
        this.mErrorView = (TextView) view.findViewById(R.id.txt_input_error);

        // display label as HTML and text to be centered horizontally
        mInputView.setGravity(Gravity.CENTER_HORIZONTAL);
        final Resources resources = view.getResources();
        mInputView.setText(ResourceUtil.getFormattedString(resources, R.string.licensing_agreement,
                "platform_name", resources.getString(R.string.platform_name)).toString());
        mInputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionListener != null) {
                    actionListener.onClickAgreement();
                }
            }
        });

        setInstructions(field.getInstructions());

        // hide error text view
        mErrorView.setVisibility(View.GONE);

        // This tag is necessary for End-to-End tests to work properly
        mInputView.setTag(mField.getName());
    }

    @Override
    public JsonElement getCurrentValue() {
        // turn text view content into a JsonElement and return it
        return new JsonPrimitive(true);
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    public boolean setRawValue(String value){
        mInputView.setText(value);
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
    public void setInstructions(@Nullable String instructions) {
        if (instructions != null && !instructions.isEmpty()) {
            mInstructionView.setVisibility(View.VISIBLE);
            mInstructionView.setText(instructions);
        }
        else {
            mInstructionView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean isValidInput() {
        // hide error as we are re-validating the input
        mErrorView.setVisibility(View.GONE);
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mInputView.setEnabled(enabled);
    }

    @Override
    public void setActionListener(IActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public View getOnErrorFocusView() {
        return mInputView;
    }
}
