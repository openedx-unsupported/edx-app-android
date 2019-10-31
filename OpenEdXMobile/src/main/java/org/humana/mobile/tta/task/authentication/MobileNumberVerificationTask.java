package org.humana.mobile.tta.task.authentication;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.authentication.MobileNumberVerificationResponse;

public class MobileNumberVerificationTask extends Task<MobileNumberVerificationResponse> {

    private Bundle parameters;

    @Inject
    LoginAPI loginAPI;

    public MobileNumberVerificationTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public MobileNumberVerificationResponse call() throws Exception {
        return loginAPI.mobileNumberVerification(parameters);
    }
}
