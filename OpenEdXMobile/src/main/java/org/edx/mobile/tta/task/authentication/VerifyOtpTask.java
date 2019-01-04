package org.edx.mobile.tta.task.authentication;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.ui.otp.model.VerifyOTPResponse;

public class VerifyOtpTask extends Task<VerifyOTPResponse> {
    private Bundle parameters;

    @Inject
    LoginAPI loginAPI;

    public VerifyOtpTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public VerifyOTPResponse call() throws Exception {
        return loginAPI.verifyOTP(parameters);
    }
}
