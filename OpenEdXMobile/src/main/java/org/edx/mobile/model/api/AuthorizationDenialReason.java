package org.edx.mobile.model.api;

import com.google.gson.annotations.SerializedName;

/**
 * Describes the reason of authorization denial for a course section/subsection/unit/component.
 */
public enum AuthorizationDenialReason {
    /**
     * Denotes that a specific course section/subsection/unit/component is not accessible due to it
     * being gated (requiring verified track/payment by the learner).
     */
    @SerializedName("Feature-based Enrollments")
    FEATURE_BASED_ENROLLMENTS
}
