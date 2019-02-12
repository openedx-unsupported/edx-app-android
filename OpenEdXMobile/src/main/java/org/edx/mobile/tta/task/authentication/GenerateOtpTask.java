package org.edx.mobile.tta.task.authentication;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.authentication.SendOTPResponse;

public class GenerateOtpTask extends Task<SendOTPResponse> {
    private Bundle parameters;

    @Inject
    LoginAPI loginAPI;

    public GenerateOtpTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public SendOTPResponse call() throws Exception {
        return loginAPI.generateOTP(parameters);
    }
}
