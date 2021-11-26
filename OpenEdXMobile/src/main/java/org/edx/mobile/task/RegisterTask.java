package org.edx.mobile.task;

import android.content.Context;
import android.os.Bundle;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.social.SocialFactory;

import javax.inject.Inject;


public abstract class RegisterTask extends Task<AuthResponse> {

    private Bundle parameters;
    private SocialFactory.SOCIAL_SOURCE_TYPE backstoreType;
    private String accessToken;

    @Inject
    LoginAPI loginAPI;

    public RegisterTask(Context context, Bundle parameters, String accessToken, SocialFactory.SOCIAL_SOURCE_TYPE backstoreType) {
        super(context);
        this.parameters = parameters;
        this.accessToken = accessToken;
        this.backstoreType = backstoreType;
    }

    @Override
    protected AuthResponse doInBackground(Void... voids) {
        try {
            switch (backstoreType) {
                case TYPE_GOOGLE:
                    return loginAPI.registerUsingGoogle(parameters, accessToken);
                case TYPE_FACEBOOK:
                    return loginAPI.registerUsingFacebook(parameters, accessToken);
                case TYPE_MICROSOFT:
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
