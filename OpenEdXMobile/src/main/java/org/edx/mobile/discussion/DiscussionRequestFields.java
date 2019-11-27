package org.edx.mobile.discussion;

import androidx.annotation.NonNull;

import org.edx.mobile.util.Config;

import java.util.Collections;
import java.util.List;

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

    /**
     * Generate the list of query param values to send for the requested_fields param.
     *
     * @param config The Config object to use for conditional param value additions.
     * @return List of requested fields for query param.
     */
    public static List<String> getRequestedFieldsList(@NonNull Config config) {
        final List<String> requestedFields;
        if (config.isDiscussionProfilePicturesEnabled()) {
            requestedFields = Collections.singletonList(
                    DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());
        } else {
            requestedFields = Collections.EMPTY_LIST;
        }
        return requestedFields;
    }
}
