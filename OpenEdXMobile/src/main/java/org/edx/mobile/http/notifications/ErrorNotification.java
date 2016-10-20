package org.edx.mobile.http.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.NetworkUtil;

import java.io.IOException;

/**
 * A user notification of errors while loading content from, or submitting content to, a remote
 * server.
 */
public abstract class ErrorNotification {
    /**
     * The logger for this class.
     */
    private final Logger logger = new Logger(ErrorNotification.class.getName());

    /**
     * Show the error notification with the message appropriate for the provided error.
     *
     * @param context The Context, to be used for checking connectivity status.
     * @param error The error that occurred while attempting to retrieve from or deliver to the
     *              remote server. This may be an {@link IOException} if the request failed due to a
     *              network failure, an {HttpResponseStatusException} if the failure was due to
     *              receiving an error code, or any {@link Throwable} implementation if one was
     *              thrown unexpectedly while creating the request or processing the response.
     */
    public void showError(@NonNull final Context context, @NonNull final Throwable error) {
        @StringRes
        int errorResId = R.string.error_unknown;
        Icon icon = null;
        @StringRes
        int actionTextResId = 0;
        View.OnClickListener actionListener = null;
        if (error instanceof IOException) {
            icon = FontAwesomeIcons.fa_wifi;
            if (NetworkUtil.isConnected(context)) {
                errorResId = R.string.network_connected_error;
            } else {
                errorResId = R.string.reset_no_network_message;
            }
        } else if (error instanceof HttpStatusException) {
            switch (((HttpStatusException) error).getStatusCode()) {
                case HttpStatus.SERVICE_UNAVAILABLE:
                    errorResId = R.string.network_service_unavailable;
                    break;
                case HttpStatus.UPGRADE_REQUIRED:
                    errorResId = R.string.app_version_unsupported;
                    actionTextResId = R.string.label_update;
                    actionListener = AppStoreUtils.OPEN_APP_IN_APP_STORE_CLICK_LISTENER;
                    break;
            }
        }
        if (errorResId == R.string.error_unknown) {
            // Submit crash report since this is an unknown type of error
            logger.error(error, true);
        }
        showError(errorResId, icon, actionTextResId, actionListener);
    }

    /**
     * Show the error notification according to the provided details.
     *
     * @param errorResId The resource ID of the error message.
     * @param icon The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener The callback to be invoked when the action button is clicked.
     */
    protected abstract void showError(@StringRes final int errorResId,
                                      @NonNull final Icon icon,
                                      @StringRes final int actionTextResId,
                                      @Nullable final View.OnClickListener actionListener);
}
