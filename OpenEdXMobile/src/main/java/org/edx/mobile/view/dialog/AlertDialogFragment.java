package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import org.edx.mobile.R;

import roboguice.fragment.RoboDialogFragment;

/**
 * Wrapper class to create a basic fragment dialog.
 */
public class AlertDialogFragment extends RoboDialogFragment {
    protected static final String ARG_TITLE = "ARG_TITLE";
    protected static final String ARG_TITLE_RES = "ARG_TITLE_RES";
    protected static final String ARG_MESSAGE = "ARG_MESSAGE";
    protected static final String ARG_MESSAGE_RES = "ARG_MESSAGE_RES";
    @Nullable
    protected ButtonAttribute positiveButtonAttr;
    @Nullable
    protected ButtonAttribute negativeButtonAttr;

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
        fragment.positiveButtonAttr = new ButtonAttribute() {
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
        };
        fragment.setArguments(arguments);
        return fragment;
    }

    public static AlertDialogFragment newInstance(@StringRes int titleResId,
                                                  @StringRes int messageResId,
                                                  @Nullable final DialogInterface.OnClickListener onPositiveClick) {
        final AlertDialogFragment fragment = new AlertDialogFragment();
        final Bundle arguments = new Bundle();
        arguments.putInt(ARG_TITLE_RES, titleResId);
        arguments.putInt(ARG_MESSAGE_RES, messageResId);
        fragment.positiveButtonAttr = new ButtonAttribute() {
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
        };
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
        fragment.positiveButtonAttr =  new ButtonAttribute() {
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
        };
        fragment.negativeButtonAttr = new ButtonAttribute() {
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
        };
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * TODO: Its a quick fix of a crash mentioned in LEARNER-1987, we have to fix it properly
         * which is explained and planned to implement in story LEARNER-2177
         */
        if (savedInstanceState != null) {
            dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final int titleResId = args.getInt(ARG_TITLE_RES);
        final int messageResId = args.getInt(ARG_MESSAGE_RES);
        final CharSequence title = titleResId != 0 ?
                getText(titleResId) : args.getString(ARG_TITLE);
        final CharSequence message = messageResId != 0 ?
                getText(messageResId) : args.getString(ARG_MESSAGE);

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (title != null) {
            alertDialog.setTitle(title);
        }
        if (positiveButtonAttr != null) {
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonAttr.getText(),
                    positiveButtonAttr.getOnClickListener());
        }
        if (negativeButtonAttr != null) {
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonAttr.getText(),
                    negativeButtonAttr.getOnClickListener());
        }
        return alertDialog;
    }

    public static abstract class ButtonAttribute {
        @Nullable
        abstract String getText();

        @Nullable
        abstract DialogInterface.OnClickListener getOnClickListener();
    }
}
