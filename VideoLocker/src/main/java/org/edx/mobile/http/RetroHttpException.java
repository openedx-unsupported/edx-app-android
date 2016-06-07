package org.edx.mobile.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RetroHttpException extends Exception {
    public RetroHttpException() {
    }

    public RetroHttpException(@Nullable String cause) {
        super(cause);
    }

    public RetroHttpException(@NonNull Throwable cause) {
        super(cause);
    }
}
