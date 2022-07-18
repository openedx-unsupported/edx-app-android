package org.edx.mobile.model.user;

import com.google.gson.annotations.SerializedName;

public enum DataType {
    @SerializedName("country")
    COUNTRY,

    @SerializedName("language")
    LANGUAGE,

    @SerializedName("boolean")
    BOOLEAN
}
