package org.edx.mobile.http;

import androidx.annotation.NonNull;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
     * Constructs a new {@code HttpResponseStatusException}.
     *
     * @param code    The Retrofit error response code.
     * @param content The Retrofit error response message
     */
    public HttpStatusException(int code, String content) {
        this(retrofit2.Response.error(code,
                ResponseBody.create(content, MediaType.parse("text/plain"))).raw());
    }

    /**
     * Constructs a new {@code HttpResponseStatusException}.
     *
     * @param code        The Retrofit error response code.
     * @param content     The Retrofit error response message
     * @param contentType The Retrofit error response MediaType
     */
    public HttpStatusException(int code, String content, String contentType) {
        this(retrofit2.Response.error(code,
                ResponseBody.create(content, MediaType.parse(contentType))).raw());
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
