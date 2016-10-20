package org.edx.mobile.http.notifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.joanzapata.iconify.Icon;

import org.edx.mobile.view.dialog.AlertDialogFragment;

/**
 * A modal dialog notification error message.
 */
public class DialogErrorNotification extends ErrorNotification {
    /**
     * The Fragment manager of the concerned Activity.
     */
    @NonNull
    private final FragmentManager fragmentManager;

    /**
     * Construct a new instance of the notification.
     *
     * @param fragmentManager The Fragment manager of the concerned Activity, to use for displaying
     *                        the DialogFragment.
     */
    public DialogErrorNotification(@NonNull final FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * Show the error notification as a modal dialog, according to the provided details.
     *
     * @param errorResId The resource ID of the error message.
     * @param icon The error icon. This is currently ignored here.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener The callback to be invoked when the action button is clicked.
     */
    @Override
    protected void showError(@StringRes final int errorResId,
                             @NonNull final Icon icon,
                             @StringRes final int actionTextResId,
                             @Nullable final View.OnClickListener actionListener) {
        AlertDialogFragment.newInstance(0, errorResId, null).show(fragmentManager, null);
    }
}
