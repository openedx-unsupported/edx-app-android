package org.edx.mobile.util.images;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.NetworkUtil;

import retrofit.RetrofitError;

public enum ErrorUtils {
    ;

    protected static final Logger logger = new Logger(ErrorUtils.class.getName());
    
    @NonNull
    public static String getErrorMessage(@NonNull Throwable ex, @NonNull Context context) {
        String errorMessage = null;
        if (ex instanceof RetroHttpException) {
            RetrofitError cause = ((RetroHttpException) ex).getCause();
            switch (cause.getKind()) {
                case NETWORK: {
                    if (NetworkUtil.isConnected(context)) {
                        errorMessage = context.getString(R.string.network_connected_error);
                    } else {
                        errorMessage = context.getString(R.string.reset_no_network_message);
                    }
                    break;
                }
                case HTTP: {
                    if (cause.getResponse() != null) {
                        final int status = cause.getResponse().getStatus();
                        if (status == 503) {
                            errorMessage = context.getString(R.string.network_service_unavailable);
                        }
                    }
                }
                case CONVERSION:
                case UNEXPECTED: {
                    // Use default message
                    break;
                }
            }
        }
        if (null == errorMessage) {
            logger.error(ex, true /* Submit crash report since this is an unknown type of error */);
            errorMessage = context.getString(R.string.error_unknown);
        }
        return errorMessage;
    }
}
