package org.edx.mobile.http.notifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.joanzapata.iconify.Icon;

import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;

/**
 * A persistent Snackbar notification error message.
 */
public class SnackbarErrorNotification extends ErrorNotification {
    /**
     * A view from the content layout.
     */
    @NonNull
    private final View view;

    /**
     * Construct a new instance of the notification.
     *
     * @param view A view from the content layout, used to seek an appropriate anchor for the
     *             Snackbar.
     */
    public SnackbarErrorNotification(@NonNull final View view) {
        this.view = view;
    }

    /**
     * Show the error notification as a persistent Snackbar, according to the provided details.
     *
     * @param errorResId The resource ID of the error message.
     * @param icon The error icon. This is ignored here, since Snackbar doesn't really support
     *             icons.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener The callback to be invoked when the action button is clicked.
     */
    @Override
    protected void showError(@StringRes final int errorResId,
                             @NonNull final Icon icon,
                             @StringRes final int actionTextResId,
                             @Nullable final View.OnClickListener actionListener) {
        final Snackbar snackbar = Snackbar.make(view, errorResId, LENGTH_INDEFINITE);
        if (actionTextResId != 0) {
            snackbar.setAction(actionTextResId, actionListener);
        }
        snackbar.show();
    }
}
