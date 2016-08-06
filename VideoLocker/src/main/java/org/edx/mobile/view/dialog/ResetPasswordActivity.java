package org.edx.mobile.view.dialog;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseAppActivity;
import org.edx.mobile.databinding.ActivityResetPasswordBinding;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.task.ResetPasswordTask;
import org.edx.mobile.util.InputValidationUtil;
import org.edx.mobile.util.IntentFactory;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ErrorUtils;

public class ResetPasswordActivity extends BaseAppActivity {

    private ActivityResetPasswordBinding binding;
    private ResetPasswordTask resetPasswordTask;

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
        if (resetPasswordTask != null) {
            resetPasswordTask.cancel(true);
            resetPasswordTask = null;
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
        resetPasswordTask = new ResetPasswordTask(this, email) {
            @Override
            public void onSuccess(@NonNull ResetPasswordResponse result) {
                if (result.isSuccess()) {
                    onResetSuccess();
                } else {
                    onResetError(result.getPrimaryReason());
                }
            }

            @Override
            public void onException(Exception ex) {
                super.onException(ex);
                onResetError(ErrorUtils.getErrorMessage(ex, context));
            }
        };
        resetPasswordTask.execute();
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