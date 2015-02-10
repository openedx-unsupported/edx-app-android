package org.edx.mobile.model.registration;

import com.google.gson.annotations.SerializedName;

public class RegistrationOption {
    private @SerializedName("name")     String name;
    private @SerializedName("value")    String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
