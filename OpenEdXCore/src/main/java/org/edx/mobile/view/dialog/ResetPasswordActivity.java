package org.edx.mobile.view.dialog;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.base.BaseAppActivity;
import org.edx.mobile.databinding.ActivityResetPasswordBinding;
import org.edx.mobile.http.CallTrigger;
import org.edx.mobile.http.ErrorHandlingCallback;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.util.InputValidationUtil;
import org.edx.mobile.util.IntentFactory;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ErrorUtils;

import retrofit2.Call;

public class ResetPasswordActivity extends BaseAppActivity {

    private ActivityResetPasswordBinding binding;
    private Call<ResetPasswordResponse> resetPasswordCall;
    @Inject
    private LoginService loginService;

    private static final String EXTRA_LOGIN_EMAIL = "login_email";

    @NonNull
    public static Intent newIntent(@Nullable String email) {
        return IntentFactory.newIntentForComponent(ResetPasswordActivity.class)
                .putExtra(EXTRA_LOGIN_EMAIL, email);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password);
        binding.positiveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });
        binding.negativeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        binding.emailEdit.setText(getIntent().getStringExtra(EXTRA_LOGIN_EMAIL));
        binding.emailEdit.setEnabled(true);
        binding.loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resetPasswordCall != null) {
            resetPasswordCall.cancel();
            resetPasswordCall = null;
        }
    }

    private void onSubmit() {
        if (!NetworkUtil.isConnected(getApplicationContext())) {
            onResetError(getString(R.string.network_not_connected_short));
            return;
        }
        final String email = binding.emailEdit.getText().toString().trim();
        if (!InputValidationUtil.isValidEmail(email)) {
            binding.emailEdit.requestFocus();
            onResetError(getString(R.string.error_invalid_email));
            return;
        }
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        binding.dialogErrorMessage.setVisibility(View.GONE);
        binding.emailEdit.setEnabled(false);
        resetPasswordCall = loginService.resetPassword(email);
        resetPasswordCall.enqueue(new ErrorHandlingCallback<ResetPasswordResponse>(
                this, CallTrigger.USER_ACTION) {
            @Override
            protected void onResponse(@NonNull final ResetPasswordResponse result) {
                if (result.isSuccess()) {
                    onResetSuccess();
                } else {
                    onResetError(result.getPrimaryReason());
                }
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                onResetError(ErrorUtils.getErrorMessage(error, ResetPasswordActivity.this));
            }
        });
    }

    private void onResetSuccess() {
        finish();
        startActivity(ResetPasswordSuccessActivity.newIntent());
    }

    private void onResetError(@NonNull String error) {
        binding.dialogErrorMessage.setText(error);
        binding.dialogErrorMessage.setVisibility(View.VISIBLE);
        binding.loadingIndicator.setVisibility(View.GONE);
        binding.emailEdit.setEnabled(true);
    }
}
