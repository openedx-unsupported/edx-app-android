package org.edx.mobile.http;

import android.support.annotation.NonNull;

import retrofit.RetrofitError;

/**
 * Thrown when a Retrofit HTTP call returns an error code. Wraps
 * around a {@link RetrofitError} as a checked exception.
 */
public class HttpResponseStatusException extends RetroHttpException {
    /**
     * Validate that the original {@link RetrofitError} is an HTTP error.
     *
     * @param cause The Retrofit exception to validate.
     * @return The provided Retrofit exception if it's valid.
     * @throws IllegalArgumentException if validation fails.
     */
    private static RetrofitError validate(@NonNull RetrofitError cause) {
        if (cause.getKind() != RetrofitError.Kind.HTTP) {
            throw new IllegalArgumentException();
        }
        return cause;
    }

    /**
     * Construct a new instance of {@link HttpResponseStatusException}
     * wrapping the provided {@link RetrofitError}.
     *
     * @param cause The original Retrofit network exception.
     */
    public HttpResponseStatusException(@NonNull RetrofitError cause) {
        super(validate(cause));
    }

    /**
     * @return The HTTP error code that caused this
     *         exception.
     */
    public int getStatusCode() {
        return getCause().getResponse().getStatus();
    }
}
