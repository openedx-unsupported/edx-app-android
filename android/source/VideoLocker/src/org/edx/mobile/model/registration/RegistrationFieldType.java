package org.edx.mobile.model.registration;

import com.google.gson.annotations.SerializedName;

public enum RegistrationFieldType {
    @SerializedName("select")
    MULTI,

    @SerializedName("password")
    PASSWORD,

    @SerializedName("email")
    EMAIL,

    @SerializedName("text")
    TEXT,

    @SerializedName("textarea")
    TEXTAREA,

    @SerializedName("checkbox")
    CHECKBOX
}
