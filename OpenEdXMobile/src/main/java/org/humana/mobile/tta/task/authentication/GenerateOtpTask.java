package org.humana.mobile.tta.task.authentication;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.authentication.SendOTPResponse;

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
