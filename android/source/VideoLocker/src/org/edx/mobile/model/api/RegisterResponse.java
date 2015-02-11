package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rohan on 2/11/15.
 */
public class RegisterResponse {

    private @SerializedName("success") boolean success = false;
    private @SerializedName("redirect_url") String redirectUrl;
    private @SerializedName("field") String field;
    private @SerializedName("value") String value;

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
}
