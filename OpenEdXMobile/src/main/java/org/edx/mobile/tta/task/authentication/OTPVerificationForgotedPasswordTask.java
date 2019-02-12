package org.edx.mobile.tta.task.authentication;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.authentication.VerifyOTPForgotedPasswordResponse;

public class OTPVerificationForgotedPasswordTask extends Task<VerifyOTPForgotedPasswordResponse> {
    private Bundle parameters;

    @Inject
    LoginAPI loginAPI;

    public OTPVerificationForgotedPasswordTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public VerifyOTPForgotedPasswordResponse call() throws Exception {
        return loginAPI.OTPVerification_For_ForgotedPassword(parameters);
    }
}
