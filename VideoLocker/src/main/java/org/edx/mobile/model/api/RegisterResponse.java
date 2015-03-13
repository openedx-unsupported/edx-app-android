package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rohan on 2/11/15.
 */
public class RegisterResponse {
    public static enum Status { NEW_ACCOUNT, EXISTING_ACCOUNT_LINKED, EXISTING_ACCOUNT_NOT_LINKED, ERROR }

    private @SerializedName("success") boolean success = false;
    private @SerializedName("redirect_url") String redirectUrl;
    private @SerializedName("field") String field;
    private @SerializedName("value") String value;
    //for local dummy test
    private @SerializedName("status") int status = 0;

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

    public RegisterResponse(){}
    //for local dummy testing
    public RegisterResponse(int status){
        this.status = status;
    }

    public Status getStatus() {
        return  Status.values()[status];
    }
}
