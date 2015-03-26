package org.edx.mobile.task;

import android.content.Context;
import android.os.Bundle;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.social.SocialFactory;

public abstract class RegisterTask extends Task<RegisterResponse> {

    private Bundle parameters;
    private AuthResponse auth;
    private SocialFactory.SOCIAL_SOURCE_TYPE backstoreType;
    private String accessToken;

    public RegisterTask(Context context, Bundle parameters, String accessToken, SocialFactory.SOCIAL_SOURCE_TYPE backstoreType) {
        super(context);
        this.parameters = parameters;
        this.accessToken = accessToken;
        this.backstoreType = backstoreType;
    }

    @Override
    protected RegisterResponse doInBackground(Object... params) {
        try {
            Api api = new Api(context);
            RegisterResponse res = api.register(parameters);

            if (res.isSuccess()) {
                switch ( backstoreType ){
                    case  TYPE_GOOGLE :
                    case  TYPE_FACEBOOK :
                        //skip auth for social registration, as we use the same
                        //access_token for registration already.
                        auth = new AuthResponse();
                        auth.access_token = this.accessToken;
                      //  auth = api.socialLogin(accessToken, backstoreType);
                         if (auth.isSuccess()) {
                             logger.debug("login succeeded after social registration");
                        }
                        break;
                    default: //normal email addrss login
                        String username = parameters.getString("username");
                        String password = parameters.getString("password");

                        auth = LoginTask.getAuthResponse(context, username, password);
                        if (auth.isSuccess()) {
                            logger.debug("login succeeded after email registration");
                        }
                }
            }

            return res;
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }

    public AuthResponse getAuth() {
        return auth;
    }
}
