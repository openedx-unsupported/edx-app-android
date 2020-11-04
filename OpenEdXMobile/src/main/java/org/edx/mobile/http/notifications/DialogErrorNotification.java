package org.edx.mobile.http.notifications;

import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import com.joanzapata.iconify.Icon;

import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.view.dialog.AlertDialogFragment;

/**
 * A modal dialog notification error message.
 */
public class DialogErrorNotification extends ErrorNotification {
    /**
     * Reference of the {@link androidx.fragment.app.Fragment} we will be displaying this error on.
     */
    @NonNull
    private final BaseFragment baseFragment;

    /**
     * Construct a new instance of the notification.
     *
     * @param baseFragment Reference of the {@link androidx.fragment.app.Fragment} to use for
     *                     displaying the {@link androidx.fragment.app.DialogFragment}.
     */
    public DialogErrorNotification(@NonNull final BaseFragment baseFragment) {
        this.baseFragment = baseFragment;
    }

    /**
     * Show the error notification as a modal dialog, according to the provided details.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon. This is currently ignored here.
     * @param actionTextResId The resource ID of the action button text.
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    @Override
    public void showError(@StringRes final int errorResId,
                          @Nullable final Icon icon,
                          @StringRes final int actionTextResId,
                          @Nullable final View.OnClickListener actionListener) {
        if (baseFragment.isResumed()) {
            AlertDialogFragment.newInstance(0, errorResId,
                    actionListener == null ? null :
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    actionListener.onClick(((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE));
                                }
                            }
            ).show(baseFragment.getChildFragmentManager(), null);
        }
    }

    /**
     * Show the error dialog according to the provided details.
     *
     * @param errorResId      The resource ID of the error message.
     * @param icon            The error icon.
     * @param actionTextResId The resource ID of the action button text.
     * @param duration        The duration of the error message visibility
     * @param actionListener  The callback to be invoked when the action button is clicked.
     */
    @Override
    public void showError(int errorResId, @Nullable Icon icon, int actionTextResId, int duration, @Nullable View.OnClickListener actionListener) {
        // Ignoring the duration as it won't be use in Dialog
        showError(errorResId, icon, actionTextResId, actionListener);
    }
}
