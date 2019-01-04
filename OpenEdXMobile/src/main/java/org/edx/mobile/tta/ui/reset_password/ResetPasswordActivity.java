package org.edx.mobile.tta.ui.reset_password;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.reset_password.view_model.ResetPasswordViewModel;

public class ResetPasswordActivity extends BaseVMActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle parameters = getIntent().getExtras();
        binding(R.layout.t_activity_reset_password, new ResetPasswordViewModel(this,
                parameters.getString(Constants.KEY_MOBILE_NUMBER), parameters.getString(Constants.KEY_OTP_TRANSACTION_ID)));
    }
}
