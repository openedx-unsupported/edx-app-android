package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

public class RegisterResponseFieldError {

    private @SerializedName("user_message") String userMessage;

    public String getUserMessage() {
        return userMessage;
    }
}
