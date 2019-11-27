package org.edx.mobile.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.databinding.ResetPasswordDialogBinding;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.interfaces.OnActivityResultListener;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.util.InputValidationUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.util.images.ErrorUtils;

import retrofit2.Call;
import roboguice.fragment.RoboDialogFragment;

import static android.app.Activity.RESULT_OK;

public class ResetPasswordDialogFragment extends RoboDialogFragment {
    private static final String ARG_LOGIN_EMAIL = "login_email";

    public static final int REQUEST_CODE = 0x5a3d7562;

    @Inject
    private LoginService loginService;

    @NonNull
    private ResetPasswordDialogBinding binding;

    @Nullable
    private Call<ResetPasswordResponse> resetCall;

    public static ResetPasswordDialogFragment newInstance(@Nullable String email) {
        ResetPasswordDialogFragment fragment = new ResetPasswordDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOGIN_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(),
                R.layout.reset_password_dialog, null, false);
        binding.emailEditText.setText(getArguments().getString(ARG_LOGIN_EMAIL));

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirm_dialog_title_help)
                .setMessage(R.string.confirm_dialog_message_help)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setView(binding.getRoot())
                .create();
        alertDialog.setCanceledOnTouchOutside(false);

        return alertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
         * There are two OnClickListeners associated with an alert dialog button:
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
        ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submit(binding.emailEditText.getText().toString().trim());
                        SoftKeyboardUtil.hide(binding.emailInputLayout);
                    }
                });
    }

    public void showError(@NonNull String error) {
        binding.emailInputLayout.setError(error);
        binding.emailInputLayout.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }

    private void submit(@Nullable String email) {
        if (!NetworkUtil.isConnected(getContext())) {
            showError(getString(R.string.network_not_connected_short));
        } else if (!InputValidationUtil.isValidEmail(email)) {
            showError(getString(R.string.error_invalid_email));
        } else {
            setUiForInteraction(false);
            binding.emailInputLayout.setError(null);
            resetCall = loginService.resetPassword(email);
            resetCall.enqueue(new ErrorHandlingCallback<ResetPasswordResponse>(getContext()) {
                @Override
                protected void onResponse(@NonNull final ResetPasswordResponse result) {
                    setUiForInteraction(true);
                    if (result.isSuccess()) {
                        OnActivityResultListener.Util.deliverResult(
                                ResetPasswordDialogFragment.this, REQUEST_CODE, RESULT_OK, null);
                        dismiss();
                    } else {
                        final String errorMsg = result.getPrimaryReason();
                        showError(errorMsg);
                    }
                }

                @Override
                protected void onFailure(@NonNull Throwable error) {
                    setUiForInteraction(true);
                    final String errorMsg = ErrorUtils.getErrorMessage(error, getContext());
                    showError(errorMsg);
                }
            });
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (resetCall != null) {
            resetCall.cancel();
        }
    }

    private void setUiForInteraction(boolean enabled) {
        if (getDialog() != null) {
            binding.emailEditText.setEnabled(enabled);
            binding.getRoot().findViewById(R.id.loading_indicator).setVisibility(enabled ? View.GONE : View.VISIBLE);
            ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(enabled);
        }
    }
}
