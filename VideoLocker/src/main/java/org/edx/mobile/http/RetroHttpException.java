package org.edx.mobile.http;

import android.support.annotation.NonNull;

import retrofit.RetrofitError;

/**
 * Should this be the base class of all http exceptions?
 */
public class RetroHttpException extends Exception {

    @NonNull
    private final RetrofitError cause;

    @NonNull
    private final int statusCode;

    public RetroHttpException(@NonNull RetrofitError cause) {
        super(cause);
        this.cause = cause;
        this.statusCode = cause.getResponse().getStatus();
    }

    @Override
    @NonNull
    public RetrofitError getCause() {
        return cause;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
