package org.edx.mobile.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class HttpException extends Exception {
    public HttpException() {
    }

    public HttpException(@Nullable String cause) {
        super(cause);
    }

    public HttpException(@NonNull Throwable cause) {
        super(cause);
    }
}
