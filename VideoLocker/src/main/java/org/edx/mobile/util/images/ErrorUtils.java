package org.edx.mobile.util.images;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.http.HttpResponseStatusException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.NetworkUtil;

import java.io.IOException;

public enum ErrorUtils {
    ;

    protected static final Logger logger = new Logger(ErrorUtils.class.getName());

    @NonNull
    public static String getErrorMessage(@NonNull Throwable ex, @NonNull Context context) {
        String errorMessage = null;
        if (ex instanceof IOException) {
            if (NetworkUtil.isConnected(context)) {
                errorMessage = context.getString(R.string.network_connected_error);
            } else {
                errorMessage = context.getString(R.string.reset_no_network_message);
            }
        } else if (ex instanceof HttpResponseStatusException) {
            final int status = ((HttpResponseStatusException) ex).getStatusCode();
            switch (status) {
                case HttpStatus.SERVICE_UNAVAILABLE:
                    errorMessage = context.getString(R.string.network_service_unavailable);
                    break;
                case HttpStatus.NOT_FOUND:
                case HttpStatus.INTERNAL_SERVER_ERROR:
                    errorMessage = context.getString(R.string.action_not_completed);
                    break;
            }
        }
        if (null == errorMessage) {
            logger.error(ex, true /* Submit crash report since this is an unknown type of error */);
            errorMessage = context.getString(R.string.error_unknown);
        }
        return errorMessage;
    }
}
