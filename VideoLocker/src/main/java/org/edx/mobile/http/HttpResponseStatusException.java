package org.edx.mobile.http;

import android.support.annotation.NonNull;

public class HttpResponseStatusException extends RetroHttpException {
    private final int statusCode;

    public HttpResponseStatusException(@NonNull Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public HttpResponseStatusException(int statusCode) {
        super(String.valueOf(statusCode));
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
