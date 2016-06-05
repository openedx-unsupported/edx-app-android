package org.edx.mobile.util.images;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.http.HttpConnectivityException;
import org.edx.mobile.http.HttpResponseStatusException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.common.BannerDisplayCallback;
import org.edx.mobile.view.common.BannerType;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskMessageCallback;

public enum ErrorUtils {
    ;

    protected static final Logger logger = new Logger(ErrorUtils.class.getName());

    @NonNull
    public static String getErrorMessage(@NonNull Throwable ex, @NonNull Context context) {
        String errorMessage = null;
        if (ex instanceof HttpConnectivityException) {
            if (NetworkUtil.isConnected(context)) {
                errorMessage = context.getString(R.string.network_connected_error);
            } else {
                errorMessage = context.getString(R.string.reset_no_network_message);
            }
        } else if (ex instanceof HttpResponseStatusException) {
            final int status = ((HttpResponseStatusException) ex).getStatusCode();
            if (status == 503) {
                errorMessage = context.getString(R.string.network_service_unavailable);
            }
        }
        if (null == errorMessage) {
            logger.error(ex, true /* Submit crash report since this is an unknown type of error */);
            errorMessage = context.getString(R.string.error_unknown);
        }
        return errorMessage;
    }

    /**
     * Deliver the appropriate error message to their callbacks.
     *
     * @param ex The exception that occurred
     * @param context A Context
     * @param taskMessageCallback A TaskMessageCallback to deliver the error to, or null if not
     *                            available.
     * @param bannerDisplayCallback A BannerDisplayCallback to display the error message as a
     *                              banner if appropriate, or null if not available.
     */
    public static void displayErrorMessage(@NonNull Throwable ex, @NonNull Context context,
                                           @Nullable TaskMessageCallback taskMessageCallback,
                                           @Nullable BannerDisplayCallback bannerDisplayCallback) {
        if (bannerDisplayCallback != null) {
            if (ex instanceof HttpConnectivityException) {
                bannerDisplayCallback.showBanner(BannerType.OFFLINE);
                return;
            }
            if (ex instanceof HttpResponseStatusException &&
                    ((HttpResponseStatusException) ex).getStatusCode() == 426) {
                bannerDisplayCallback.showBanner(BannerType.OFFLINE);
                return;
            }
        }

        if (taskMessageCallback != null) {
            taskMessageCallback.onMessage(MessageType.FLYIN_ERROR, getErrorMessage(ex, context));
        }
    }
}
