package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.edx.mobile.R;

/**
 * Note: This is a very simple implementation that only shows an {@link AlertDialog} with a given
 * message and an OK button without any listener.
 *
 * In the future, more customizability might be added as need.
  */

public class AlertDialogFragment extends DialogFragment {
    public static final String ARG_MESSAGE = "message";

    public static AlertDialogFragment newInstance(String message) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(R.string.label_ok, null)
                .create();
    }
}
