package org.edx.mobile.http;

import androidx.annotation.NonNull;

import okhttp3.Response;

/**
 * Signals an HTTP status error.
 */
public class HttpStatusException extends Exception {
    @NonNull
    private final Response response;

    /**
     * Constructs a new {@code HttpResponseStatusException}.
     *
     * @param response The error response.
     */
    public HttpStatusException(@NonNull final Response response) {
        // Populate the message argument of the parent exception with all the response data we have
        super(response.toString());
        this.response = response;
    }

    /**
     * Constructs a new {@code HttpResponseStatusException}.
     *
     * @param response The Retrofit error response.
     */
    public HttpStatusException(@NonNull final retrofit2.Response<?> response) {
        this(response.raw());
    }

    /**
     * @return The error response.
     */
    @NonNull
    public Response getResponse() {
        return response;
    }

    /**
     * @return The HTTP status error code.
     */
    public int getStatusCode() {
        return response.code();
    }
}
