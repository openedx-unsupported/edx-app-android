package org.edx.mobile.model.registration;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohan on 2/10/15.
 */
public class RegistrationForm {

    private @SerializedName("method") String method;
    private @SerializedName("submit_url") String submitUrl;
    private @SerializedName("fields") List<RegistrationFormField> fields;

    public List<RegistrationFormField> getFields() {
        if (fields == null) {
            return new ArrayList<>();
        }
        return fields;
    }

    public String getMethod() {
        return method;
    }

    public String getSubmitUrl() {
        return submitUrl;
    }
}
