package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.module.registration.view.IRegistrationFieldView;

import java.util.List;

public class RegisterResponse {
    private @SerializedName("success") boolean success = false;
    private @SerializedName("redirect_url") String redirectUrl;
    private @SerializedName("field") String field;
    private @SerializedName("value") String value;

    private FormFieldMessageBody messageBody;

    public boolean isSuccess() {
        return success;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public FormFieldMessageBody getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(FormFieldMessageBody messageBody) {
        this.messageBody = messageBody;
    }
}
