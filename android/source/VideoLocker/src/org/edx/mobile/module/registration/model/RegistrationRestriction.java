package org.edx.mobile.module.registration.model;

import com.google.gson.annotations.SerializedName;

public class RegistrationRestriction {
    private @SerializedName("min_length")    int minLength = -1;
    private @SerializedName("max_length")    int maxLength = -1;

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
