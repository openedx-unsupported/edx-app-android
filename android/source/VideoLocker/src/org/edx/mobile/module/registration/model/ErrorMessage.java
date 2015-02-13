package org.edx.mobile.module.registration.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rohan on 2/11/15.
 */
public class ErrorMessage {

    private @SerializedName("required") String required;
    private @SerializedName("min_length") String minLength;
    private @SerializedName("max_length") String maxLength;

    public String getRequired() {
        return required;
    }

    public String getMinLength() {
        return minLength;
    }

    public String getMaxLength() {
        return maxLength;
    }
}
