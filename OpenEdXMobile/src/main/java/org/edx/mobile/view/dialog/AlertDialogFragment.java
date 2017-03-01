package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.edx.mobile.R;

import roboguice.fragment.RoboDialogFragment;

/**
 * Wrapper class to create a basic fragment dialog.
 */
public class AlertDialogFragment extends RoboDialogFragment {
    protected static final String ARG_TITLE = "ARG_TITLE";
    protected static final String ARG_MESSAGE = "ARG_MESSAGE";
    protected static final String ARG_POSITIVE_ATTR = "ARG_POSITIVE_ATTR";
    protected static final String ARG_NEGATIVE_ATTR = "ARG_NEGATIVE_ATTR";

    /**
     * Creates a new instance of simple dialog that shows message, could have title and will have
     * only positive button with 'OK' text.
     *
     * @param title           Title of dialog.
     * @param message         Message of dialog.
     * @param onPositiveClick Positive button click listener.
     * @return New instance of dialog.
     */
    public static AlertDialogFragment newInstance(final @Nullable String title,
                                                  final @NonNull String message,
                                                  final @Nullable DialogInterface.OnClickListener onPositiveClick) {
        final AlertDialogFragment fragment = new AlertDialogFragment();
        // Supply params as an argument.
        final Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_MESSAGE, message);
        arguments.putParcelable(ARG_POSITIVE_ATTR, new ButtonAttribute() {
            @NonNull
            @Override
            public String getText() {
                return fragment.getResources().getString(R.string.label_ok);
            }

            @Nullable
            @Override
            public DialogInterface.OnClickListener getOnClickListener() {
                return onPositiveClick;
            }
        });
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Creates a new instance of dialog that shows message, could have title, will have a positive
     * button with given text and also could have negative button with given text.
     *
     * @param title           Title of dialog.
     * @param message         Message of dialog.
     * @param positiveText    Positive button text.
     * @param onPositiveClick Positive button click listener.
     * @param negativeText    Negative button text.
     * @param onNegativeClick Negative button click listener.
     * @return New instance of dialog.
     */
    public static AlertDialogFragment newInstance(final @Nullable String title,
                                                  final @NonNull String message,
                                                  final @NonNull String positiveText,
                                                  final @Nullable DialogInterface.OnClickListener onPositiveClick,
                                                  final @Nullable String negativeText,
                                                  final @Nullable DialogInterface.OnClickListener onNegativeClick) {
        final AlertDialogFragment fragment = new AlertDialogFragment();
        // Supply params as an argument.
        final Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_MESSAGE, message);
        arguments.putParcelable(ARG_POSITIVE_ATTR, new ButtonAttribute() {
            @NonNull
            @Override
            public String getText() {
                return positiveText;
            }

            @Nullable
            @Override
            public DialogInterface.OnClickListener getOnClickListener() {
                return onPositiveClick;
            }
        });
        arguments.putParcelable(ARG_NEGATIVE_ATTR, new ButtonAttribute() {
            @NonNull
            @Override
            public String getText() {
                return negativeText;
            }

            @Nullable
            @Override
            public DialogInterface.OnClickListener getOnClickListener() {
                return onNegativeClick;
            }
        });
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);
        ButtonAttribute positiveButtonAttr = getArguments().getParcelable(ARG_POSITIVE_ATTR);
        ButtonAttribute negativeButtonAttr = getArguments().getParcelable(ARG_NEGATIVE_ATTR);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(positiveButtonAttr.getText(), positiveButtonAttr.getOnClickListener())
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (title != null) {
            alertDialog.setTitle(title);
        }
        if (negativeButtonAttr != null) {
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonAttr.getText(),
                    negativeButtonAttr.getOnClickListener());
        }
        return alertDialog;
    }

    public static abstract class ButtonAttribute implements Parcelable {
        @Nullable
        abstract String getText();

        @Nullable
        abstract DialogInterface.OnClickListener getOnClickListener();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }
}
