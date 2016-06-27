package org.edx.mobile.http;

import android.support.annotation.NonNull;

import retrofit.RetrofitError;

/**
 * Checked exception wrapper around
 * {@link RetrofitError}. Specific error types may
 * be represented by specific subclasses with
 * appropriate convenience methods.
 */
public class RetroHttpException extends Exception {
    /**
     * Construct a new instance of {@link RetroHttpException}
     * wrapping the provided {@link RetrofitError}.
     *
     * @param cause The original Retrofit network exception.
     */
    public RetroHttpException(@NonNull RetrofitError cause) {
        super(cause);
    }

    /**
     * @return The {@link RetrofitError} that is being
     *         wrapped by this class.
     */
    @Override
    @NonNull
    public RetrofitError getCause() {
        return (RetrofitError) super.getCause();
    }
}
