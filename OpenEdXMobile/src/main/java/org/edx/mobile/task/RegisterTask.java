package org.edx.mobile.task;

import android.content.Context;
import android.os.Bundle;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.model.authentication.AuthResponse;
import org.edx.mobile.social.SocialSourceType;

import dagger.hilt.android.EntryPointAccessors;

public abstract class RegisterTask extends Task<AuthResponse> {

    private Bundle parameters;
    private SocialSourceType backstoreType;
    private String accessToken;
    private LoginAPI loginAPI;

    public RegisterTask(Context context, Bundle parameters, String accessToken, SocialSourceType backstoreType) {
        super(context);
        this.parameters = parameters;
        this.accessToken = accessToken;
        this.backstoreType = backstoreType;
        loginAPI = EntryPointAccessors.fromApplication(
                context, EdxDefaultModule.ProviderEntryPoint.class).getLoginAPI();
    }

    @Override
    protected AuthResponse doInBackground(Void... voids) {
        try {
            switch (backstoreType) {
                case GOOGLE:
                    return loginAPI.registerUsingGoogle(parameters, accessToken);
                case FACEBOOK:
                    return loginAPI.registerUsingFacebook(parameters, accessToken);
                case MICROSOFT:
                    return loginAPI.registerUsingMicrosoft(parameters, accessToken);
            }
            // normal email address login
            return loginAPI.registerUsingEmail(parameters);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }
}
