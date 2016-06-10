package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.services.ServiceManager;

import javax.inject.Inject;

public abstract class ResetPasswordTask extends Task<ResetPasswordResponse> {

    @Inject
    LoginAPI loginAPI;

    @NonNull
    String emailId;

    public ResetPasswordTask(@NonNull Context context, @NonNull String emailId) {
        super(context);
        this.emailId = emailId;
    }

    @Override
    public ResetPasswordResponse call() throws Exception{
        return loginAPI.resetPassword(emailId);
    }
}
