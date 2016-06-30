package org.edx.mobile.http;

import android.support.annotation.NonNull;

public class HttpConnectivityException extends RetroHttpException {
    public HttpConnectivityException(@NonNull Throwable cause) {
        super(cause);
    }
}
