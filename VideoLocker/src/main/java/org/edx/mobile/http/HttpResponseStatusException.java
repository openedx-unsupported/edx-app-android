package org.edx.mobile.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class HttpResponseStatusException extends RetroHttpException {
    private final int statusCode;
    @Nullable
    private final String body;

    public HttpResponseStatusException(@NonNull Throwable cause, int statusCode, @Nullable String body) {
        super(cause);
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Nullable
    public String getBody() {
        return body;
    }
}
