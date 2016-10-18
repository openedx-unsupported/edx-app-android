package org.edx.mobile.http.notifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import com.joanzapata.iconify.Icon;

/**
 * A user notification of errors while loading content from, or submitting content to, a remote
 * server.
 */
public interface ErrorNotification {
    /**
     * Show the error notification.
     *
     * @param errorResId The resource ID of the error message.
     * @param icon The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener The callback to be invoked when the action button is clicked.
     */
    void showError(@StringRes final int errorResId,
                   @NonNull final Icon icon,
                   @StringRes final int actionTextResId,
                   @Nullable final View.OnClickListener actionListener);
}
