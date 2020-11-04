package org.edx.mobile.http.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.view.View;

import com.joanzapata.iconify.Icon;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.images.ErrorUtils;

import java.io.IOException;

/**
 * A notification to show errors while loading content from or submitting content to a remote server.
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
     * @param error   The error that occurred while attempting to retrieve from or deliver to the
     *                remote server. This may be an {@link IOException} if the request failed due to a
     *                network failure, an {HttpResponseStatusException} if the failure was due to
     *                receiving an error code, or any {@link Throwable} implementation if one was
     *                thrown unexpectedly while creating the request or processing the response.
     */
    public void showError(@NonNull final Context context, @NonNull final Throwable error) {
        showError(context, error, 0, null);
    }


    /**
     * Show the error notification with the message appropriate for the provided error.
     *
     * @param context         The Context, to be used for checking connectivity status.
     * @param error           The error that occurred while attempting to retrieve from or deliver to the
     *                        remote server. This may be an {@link IOException} if the request failed due to a
     *                        network failure, an {HttpResponseStatusException} if the failure was due to
     *                        receiving an error code, or any {@link Throwable} implementation if one was
     *                        thrown unexpectedly while creating the request or processing the response.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    public void showError(@NonNull final Context context, @NonNull final Throwable error,
                          @StringRes int actionTextResId,
                          @Nullable View.OnClickListener actionListener) {
        @StringRes
        final int errorResId = ErrorUtils.getErrorMessageRes(context, error, this);
        final Icon icon = ErrorUtils.getErrorIcon(error);

        if (errorResId == R.string.app_version_unsupported) {
            actionTextResId = R.string.label_update;
            actionListener = AppStoreUtils.OPEN_APP_IN_APP_STORE_CLICK_LISTENER;
        }
        showError(errorResId, icon, actionTextResId, actionListener);
    }

    /**
     * Show the error notification according to the provided details.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    public abstract void showError(@StringRes final int errorResId,
                                   @Nullable final Icon icon,
                                   @StringRes final int actionTextResId,
                                   @Nullable final View.OnClickListener actionListener);

    /**
     * Show the error notification according to the provided details.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param duration        The duration of the error message visibility
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    public abstract void showError(@StringRes final int errorResId,
                                   @Nullable final Icon icon,
                                   @StringRes final int actionTextResId,
                                   final int duration,
                                   @Nullable final View.OnClickListener actionListener);
}
