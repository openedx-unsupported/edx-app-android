package org.edx.mobile.http;

import android.support.annotation.NonNull;

import retrofit.RetrofitError;

public class RetroHttpException extends RuntimeException {
    public RetroHttpException(@NonNull Throwable cause) {
        super(cause);
    }
}
