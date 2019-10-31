package org.humana.mobile.tta.task.authentication;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.authentication.ResetForgotedPasswordResponse;

public class ResetForgotedPasswordTask extends Task<ResetForgotedPasswordResponse> {
    private Bundle parameters;

    @Inject
    LoginAPI loginAPI;

    public ResetForgotedPasswordTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public ResetForgotedPasswordResponse call() throws Exception {
        return loginAPI.ResetForgotedPassword(parameters);
    }
}
