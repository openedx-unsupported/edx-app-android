package org.edx.mobile.http;

import android.support.annotation.NonNull;

import retrofit2.Response;

/**
 * Signals an HTTP status error.
 */
public class HttpResponseStatusException extends Exception {
    @NonNull
    private final Response<?> response;

    /**
     * Constructs a new {@code HttpResponseStatusException}.
     *
     * @param response The error response.
     */
    public HttpResponseStatusException(@NonNull final Response<?> response) {
        this.response = response;
    }

    /**
     * @return The error response.
     */
    @NonNull
    public Response<?> getResponse() {
        return response;
    }

    /**
     * @return The HTTP status error code.
     */
    public int getStatusCode() {
        return response.code();
    }
}
