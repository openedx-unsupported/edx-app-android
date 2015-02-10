package org.edx.mobile.model.registration;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rohan on 2/11/15.
 */
public class ErrorMessage {

    private @SerializedName("required") String required;

    public String getRequired() {
        return required;
    }
}
