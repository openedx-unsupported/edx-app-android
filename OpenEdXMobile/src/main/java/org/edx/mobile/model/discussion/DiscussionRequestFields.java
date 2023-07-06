package org.edx.mobile.model.discussion;

import androidx.annotation.NonNull;

/**
 * Optional request fields in discussion responses and comments APIs.
 */
public enum DiscussionRequestFields {
    PROFILE_IMAGE("profile_image");

    private final String queryParamValue;

    DiscussionRequestFields(@NonNull String queryParamValue) {
        this.queryParamValue = queryParamValue;
    }

    /**
     * Get the value of the query parameter.
     *
     * @return The query parameter string.
     */
    public String getQueryParamValue() {
        return queryParamValue;
    }
}
