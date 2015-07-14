package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lindaliu on 7/14/15.
 */
public enum StartType {
    @SerializedName("string")
    STRING_START,
    @SerializedName("timestamp")
    TIMESTAMP_START,
    @SerializedName("empty")
    NONE_START
}
