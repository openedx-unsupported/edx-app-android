package org.edx.mobile.http;

import android.support.annotation.NonNull;

import java.io.IOException;

import retrofit.RetrofitError;

/**
 * Thrown when a Retrofit HTTP call fails due to connectivity
 * issues. Wraps around a {@link RetrofitError} as a checked
 * exception.
 */
public class HttpConnectivityException extends RetroHttpException {
    /**
     * Validate that the original {@link RetrofitError} is a network
     * error.
     *
     * @param cause The Retrofit exception to validate.
     * @return The provided Retrofit exception if it's valid.
     * @throws IllegalArgumentException if validation fails.
     */
    private static RetrofitError validate(@NonNull RetrofitError cause) {
        if (cause.getKind() != RetrofitError.Kind.NETWORK) {
            throw new IllegalArgumentException();
        }
        return cause;
    }

    /**
     * Construct a new instance of {@link HttpConnectivityException}
     * wrapping the provided {@link RetrofitError}.
     *
     * @param cause The original Retrofit network exception.
     */
    public HttpConnectivityException(@NonNull RetrofitError cause) {
        super(validate(cause));
    }

    /**
     * @return The initial {@link IOException} that
     *         was the cause of the wrapped
     *         {@link RetrofitError}.
     */
    @NonNull
    public IOException getRealCause() {
        return (IOException) getCause().getCause();
    }
}
