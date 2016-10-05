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
        void onDismissed();
    }

    @Inject
    private LoginService loginService;

    protected static final String EXTRA_LOGIN_EMAIL = "login_email";

    private TextInputEditText editText;
    private TextInputLayout textInputLayout;
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

        editText = new TextInputEditText(getContext());
        textInputLayout = new TextInputLayout(getContext());
        textInputLayout.addView(editText);
        textInputLayout.setHint(getContext().getString(R.string.email));

        int paddingWidth = Math.round(getResources().getDimension(R.dimen.alert_dialog_padding_width));
        int paddingHeight = Math.round(getResources().getDimension(R.dimen.alert_dialog_padding_height));
        textInputLayout.setPadding(paddingWidth, paddingHeight, paddingWidth, paddingHeight);

        alertDialog.setView(textInputLayout);
        editText.setText(getArguments().getString(EXTRA_LOGIN_EMAIL));

        return alertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        /** There are two OnClickListeners associated with an alert dialog button:
         * DialogInterface.OnClickListener and View.OnClickListener.
         *
         * DialogInterface.OnClickListener defines what clicking the button
         * does before the normal flow takes over (before the dialog is auto-dismissed)
         * This is what is generally set in AlertDialog.Builder.
         *
         * View.OnClickListener is the listener for the complete behavior of the button.
         * This has to be set by finding the button (after it has been created),
         * and overriding it. If you want to override the normal button click flow,
         * do so by overriding View.OnClickListener.
         *
         * We want to prevent the dialog from automatically closing on positive button click,
         * and letting submit() control the logic so, we are overriding View.OnClickListener here.
         */
        ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit(editText.getText().toString().trim());
            }
        });
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener!=null) {
            listener.onDismissed();
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void showError(@NonNull String error) {
        textInputLayout.setError(error);
        textInputLayout.setErrorEnabled(true);
        textInputLayout.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
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

                /**
                 * Set null for now. We override the behavior on View.OnClickListener in OnCreate.
                 * Refer the comment in onCreate() for more details.
                 */
                return null;
            }
        };
    }
}
