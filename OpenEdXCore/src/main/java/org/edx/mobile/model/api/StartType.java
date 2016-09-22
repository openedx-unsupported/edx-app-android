package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lindaliu on 7/14/15.
 */
public enum StartType {
    /**
     * Course's start date is provided as an unformatted string in {@link CourseEntry#start_display}
     */
    @SerializedName("string")STRING,
    /**
     * Course's start date is provided as a date-formatted string in {@link CourseEntry#start}
     */
    @SerializedName("timestamp")TIMESTAMP,
    /**
     * Course's start date is unset
     */
    @SerializedName("empty")EMPTY
}
