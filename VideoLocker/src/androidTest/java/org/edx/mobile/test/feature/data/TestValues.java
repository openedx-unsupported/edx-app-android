package org.edx.mobile.test.feature.data;

public enum TestValues {
    ;

    public static final Credentials ACTIVE_USER_CREDENTIALS = new Credentials("honor@example.com", "edx");
    public static final String DUMMY_PROFILE_JSON = "{\"id\": 1, \"username\": \"user\", \"email\": \"email@example.com\", \"name\": \"name\", \"course_enrollments\": \"https://mobile-devi.sandbox.edx.org/api/mobile/v0.5/users/staff/course_enrollments/\"}";
    public static final String INVALID_AUTH_JSON = "{\"access_token\": \"I am an invalid token\", \"token_type\": \"Bearer\", \"expires_in\": 2591999, \"scope\": \"\"}";

}
