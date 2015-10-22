package org.edx.mobile.user;

import com.google.gson.annotations.SerializedName;

public class FormOption {
    private @SerializedName("name")     String name;
    private @SerializedName("value")    String value;

    public FormOption() {
    }

    public FormOption(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
