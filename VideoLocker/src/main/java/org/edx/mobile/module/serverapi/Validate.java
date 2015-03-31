package org.edx.mobile.module.serverapi;

import org.edx.mobile.util.InputValidationUtil;

/**
 * Created by rohan on 2/17/15.
 */
class Validate {

    public static void emailAddress(String email) {
        if ( !InputValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Email address invalid");
        }
    }

    public static void httpResponse(int statusCode, int expectedStatusCode) {
        if (statusCode != expectedStatusCode) {
            throw new IllegalArgumentException(String.format("Expected HTTP %d, but found HTTP %d", expectedStatusCode, statusCode));
        }
    }
}
