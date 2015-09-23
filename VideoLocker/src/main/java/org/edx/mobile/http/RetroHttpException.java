package org.edx.mobile.http;

import android.support.annotation.NonNull;

import retrofit.RetrofitError;

/**
 * Should this be the base class of all http exceptions?
 */
public class RetroHttpException extends Exception {
    @NonNull
    private final RetrofitError cause;

    public RetroHttpException(@NonNull RetrofitError cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    @NonNull
    public RetrofitError getCause() {
        return cause;
    }
}
