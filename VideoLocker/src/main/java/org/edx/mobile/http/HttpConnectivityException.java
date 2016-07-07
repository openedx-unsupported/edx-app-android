package org.edx.mobile.http;

import android.support.annotation.NonNull;

public class HttpConnectivityException extends HttpException {
    public HttpConnectivityException(@NonNull Throwable cause) {
        super(cause);
    }
}
