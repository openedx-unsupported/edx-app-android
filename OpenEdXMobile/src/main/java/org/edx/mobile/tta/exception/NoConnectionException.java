package org.edx.mobile.tta.exception;

import android.content.Context;

import org.edx.mobile.R;

public class NoConnectionException extends TaException {
    public NoConnectionException(Context context) {
        super(context.getString(R.string.no_connection_exception));
    }
}
