package org.edx.mobile.task;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.module.registration.model.RegistrationDescription;

public abstract class GetRegistrationDescriptionTask extends Task<RegistrationDescription> {
    @Inject
    LoginAPI loginAPI;

    public GetRegistrationDescriptionTask(Context context) {
        super(context);
    }

    @Override
    public RegistrationDescription call() throws Exception {
        return loginAPI.getRegistrationDescription();
    }
}

