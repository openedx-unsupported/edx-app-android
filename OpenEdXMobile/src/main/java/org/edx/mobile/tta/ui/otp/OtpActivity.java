package org.edx.mobile.tta.ui.otp;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.otp.view_model.OtpViewModel;

import static org.edx.mobile.tta.Constants.KEY_MOBILE_NUMBER;
import static org.edx.mobile.tta.Constants.KEY_OTP_SOURCE;
import static org.edx.mobile.tta.Constants.KEY_PASSWORD;

public class OtpActivity extends BaseVMActivity {

    private OtpViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle parameters = getIntent().getExtras();
        viewModel = new OtpViewModel(this,
                parameters.getString(KEY_MOBILE_NUMBER),
                parameters.getString(KEY_PASSWORD, null),
                parameters.getString(KEY_OTP_SOURCE)
        );
        viewModel.registerEventBus();
        binding(R.layout.t_activity_otp, viewModel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
