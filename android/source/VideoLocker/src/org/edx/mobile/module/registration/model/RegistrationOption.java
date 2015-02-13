package org.edx.mobile.module.registration.model;

import com.google.gson.annotations.SerializedName;

public class RegistrationOption {
    private @SerializedName("default")     boolean defaultValue;
    private @SerializedName("name")     String name;
    private @SerializedName("value")    String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return name;
    }
}
