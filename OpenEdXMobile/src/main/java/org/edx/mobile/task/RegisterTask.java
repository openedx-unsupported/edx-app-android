package org.edx.mobile.task;

import android.content.Context;
import android.os.Bundle;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.model.authentication.AuthResponse;
import org.edx.mobile.social.SocialAuthSource;

import dagger.hilt.android.EntryPointAccessors;

public abstract class RegisterTask extends Task<AuthResponse> {

    private Bundle parameters;
    private SocialAuthSource socialAuthSource;
    private String accessToken;
    private LoginAPI loginAPI;

    public RegisterTask(Context context, Bundle parameters, String accessToken, SocialAuthSource socialAuthSource) {
        super(context);
        this.parameters = parameters;
        this.accessToken = accessToken;
        this.socialAuthSource = socialAuthSource;
        loginAPI = EntryPointAccessors.fromApplication(
                context, EdxDefaultModule.ProviderEntryPoint.class).getLoginAPI();
    }

    @Override
    protected AuthResponse doInBackground(Void... voids) {
        try {
            return switch (socialAuthSource) {
                case GOOGLE -> loginAPI.registerUsingGoogle(parameters, accessToken);
                case FACEBOOK -> loginAPI.registerUsingFacebook(parameters, accessToken);
                case MICROSOFT -> loginAPI.registerUsingMicrosoft(parameters, accessToken);
                default -> loginAPI.registerUsingEmail(parameters);
            };
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }
}
