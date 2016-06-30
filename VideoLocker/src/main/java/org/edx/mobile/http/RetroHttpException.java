package org.edx.mobile.http;

import android.support.annotation.NonNull;

import retrofit.RetrofitError;

public class RetroHttpException extends Exception {
    public RetroHttpException(@NonNull Throwable cause) {
        super(cause);
    }
}
