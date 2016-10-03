package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import org.edx.mobile.R;

/**
 * Note: This is a very simple implementation that only shows an {@link AlertDialog} with a given
 * message and an OK button without any listener.
 * <p/>
 * In the future, more customizability might be added as need.
 */

public class AlertDialogFragment extends DialogFragment {
    public static final String ARG_TITLE = "title";
    public static final String ARG_MESSAGE = "message";

    /**
     * Creates a new instance of this dialog fragment.
     *
     * @param title   The title.
     * @param message The message.
     * @return AlertDialogFragment object.
     * @see #showDialog(FragmentManager, String, String)
     */
    private static AlertDialogFragment newInstance(@Nullable String title, @NonNull String message) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates and shows a new instance of this dialog fragment.
     *
     * @param title   The title.
     * @param message The message.
     */
    public static void showDialog(@NonNull FragmentManager manager,
                                  @Nullable String title, @NonNull String message) {

        // TODO: Re-work this code after migrating to an MVP framework.
        try {
            newInstance(title, message).show(manager, null);
        } catch (IllegalStateException e) {
            // Do nothing, since this is an issue due to improper session management in our tasks.
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(R.string.label_ok, null)
                .create();
        if (title != null) {
            alertDialog.setTitle(title);
        }

        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }
}
