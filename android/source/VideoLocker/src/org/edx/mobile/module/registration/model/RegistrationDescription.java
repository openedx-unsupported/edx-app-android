package org.edx.mobile.module.registration.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RegistrationDescription {
    private @SerializedName("submit_url")     String endpoint;
    private @SerializedName("method")       String method;
    private @SerializedName("fields")       List<RegistrationFormField> fields;

    public String getEndpoint() {
        return endpoint;
    }

    public String getMethod() {
        return method;
    }

    public List<RegistrationFormField> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }
}
