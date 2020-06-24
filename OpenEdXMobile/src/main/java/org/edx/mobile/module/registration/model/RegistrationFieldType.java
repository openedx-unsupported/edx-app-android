package org.edx.mobile.module.registration.model;

import com.google.gson.annotations.SerializedName;

public enum RegistrationFieldType {
    @SerializedName("select")
    MULTI,

    @SerializedName("password")
    PASSWORD,

    @SerializedName("email")
    EMAIL,

    @SerializedName("confirm_email")
    CONFIRM_EMAIL,

    @SerializedName("text")
    TEXT,

    @SerializedName("textarea")
    TEXTAREA,

    @SerializedName("plaintext")
    PLAINTEXT,

    UNKNOWN
}
