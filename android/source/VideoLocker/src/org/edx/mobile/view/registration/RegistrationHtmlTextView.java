package org.edx.mobile.view.registration;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.registration.RegistrationFormField;
import org.edx.mobile.util.BrowserUtil;

class RegistrationHtmlTextView implements IRegistrationFieldView {

    protected static final Logger logger = new Logger(RegistrationHtmlTextView.class);
    protected RegistrationFormField mField;
    private View mView;
    protected TextView mInputView;
    private TextView mErrorView, mInstructionView;

    public RegistrationHtmlTextView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (TextView) view.findViewById(R.id.txt_input);
        this.mErrorView = (TextView) view.findViewById(R.id.txt_input_error);
        this.mInstructionView = (TextView) view.findViewById(R.id.txt_input_instruction);

        // display label as HTML and text to be centered horizontally
        mInputView.setMovementMethod(LinkMovementMethod.getInstance());
        mInputView.setGravity(Gravity.CENTER_HORIZONTAL);
        mInputView.setText(Html.fromHtml(
                String.format("<a href=%s>%s</a>", mField.getAgreement().getLink(), mField.getAgreement().getText())));

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
        return new JsonPrimitive(true);
    }

    @Override
    public boolean hasValue() {
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
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mInputView.setEnabled(enabled);
    }
}
