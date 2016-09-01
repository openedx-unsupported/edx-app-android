package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.http.CallTrigger;
import org.edx.mobile.http.ErrorHandlingCallback;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.util.InputValidationUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ErrorUtils;

import roboguice.RoboGuice;

public class ResetPasswordAlertDialogFragment extends AlertDialogFragment {

    public interface Listener {
        void onResult(boolean result, @Nullable String errorMessage);
        void omDismissed();
    }

    @Inject
    private LoginService loginService;

    protected static final String EXTRA_LOGIN_EMAIL = "login_email";

    private TextInputEditText et;
    private TextInputLayout til;
    private Listener listener;

    public static ResetPasswordAlertDialogFragment newInstance(@NonNull Context context, @Nullable String email) {
        ResetPasswordAlertDialogFragment fragment = new ResetPasswordAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, context.getResources().getString(R.string.confirm_dialog_title_help));
        args.putString(ARG_MESSAGE, context.getResources().getString(R.string.confirm_dialog_message_help));
        args.putString(EXTRA_LOGIN_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog alertDialog = (AlertDialog) super.onCreateDialog(savedInstanceState);
        RoboGuice.injectMembers(getContext(), this);

        et = new TextInputEditText(getContext());
        til = new TextInputLayout(getContext());
        til.addView(et);
        til.setHint(getContext().getString(R.string.email));

        int paddingWidth = Math.round(getResources().getDimension(R.dimen.alert_dialog_padding_width));
        int paddingHeight = Math.round(getResources().getDimension(R.dimen.alert_dialog_padding_height));
        til.setPadding(paddingWidth, paddingHeight, paddingWidth, paddingHeight);

        alertDialog.setView(til);

        String email = getArguments().getString(EXTRA_LOGIN_EMAIL);
        if (email != null && !email.isEmpty()) {
            et.setText(email);
        }
        return alertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        /**
         * This is a way to prevent the alert dialog from auto-dismissing when the positive/negative button is pressed
         * We find the positive button (must be after it is created), then manually override the onClickListener
         */
        ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit(getEnteredEmail());
            }
        });
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener!=null) {
            listener.omDismissed();
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Nullable
    protected String getEnteredEmail() {
        String email = null;

        if (et != null) {
            email = et.getText().toString().trim();
        }

        return email;
    }

    /**
     * Shows or hides error
     * @param error The error to show. If null, error will be hidden
     */
    public void showError(@Nullable String error) {
        if (til != null) {
            if (error != null) {
                til.setError(error);
                til.setErrorEnabled(true);
                til.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            } else {
                til.setErrorEnabled(false);
            }
        }
    }

    private void submit(@Nullable String email) {
        if (!NetworkUtil.isConnected(getContext())) {
            showError(getString(R.string.network_not_connected_short));
        } else if (!InputValidationUtil.isValidEmail(email)) {
            showError(getString(R.string.error_invalid_email));
        } else {
            loginService.resetPassword(email).enqueue(new ErrorHandlingCallback<ResetPasswordResponse>(
                    getContext(), CallTrigger.USER_ACTION) {
                @Override
                protected void onResponse(@NonNull final ResetPasswordResponse result) {
                    if (listener!=null) {
                        if (result.isSuccess()) {
                            listener.onResult(true, null);
                        }
                        else {
                            listener.onResult(false, result.getPrimaryReason());
                        }
                    }
                }

                @Override
                protected void onFailure(@NonNull Throwable error) {
                    if (listener != null) {
                        listener.onResult(false, ErrorUtils.getErrorMessage(error, getContext()));
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    protected ButtonAttributes getNegativeButtonAttributes() {
        return new ButtonAttributes() {
            @NonNull
            @Override
            public String getMessage() {
                return getContext().getResources().getString(R.string.label_cancel);
            }

            @Nullable
            @Override
            public DialogInterface.OnClickListener getOnClickListener() {
                return null;
            }
        };
    }

    @NonNull
    @Override
    protected ButtonAttributes getPositiveButtonAttributes() {
        return new ButtonAttributes() {
            @NonNull
            @Override
            public String getMessage() {
                return getContext().getResources().getString(R.string.label_ok);
            }

            @Nullable
            @Override
            public DialogInterface.OnClickListener getOnClickListener() {
                /** just set to null here, we have to find the positive button and override
                 * its onClickListener to prevent the dialog from being auto-dismissed */
                return null;
            }
        };
    }
}
