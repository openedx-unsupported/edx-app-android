package org.edx.mobile.task;

import android.content.Context;
import android.os.Bundle;

import org.edx.mobile.R;
import org.edx.mobile.exception.LoginErrorMessage;
import org.edx.mobile.exception.LoginException;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.module.prefs.PrefManager;
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
                         // do SOCIAL LOGIN first
                        if ( backstoreType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK ) {
                            auth = api.loginByFacebook(accessToken);
                        } else if ( backstoreType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE ) {
                            auth = api.loginByGoogle(accessToken);
                        }
                        if (auth != null && auth.isSuccess()) {
                            // we got a valid accessToken so profile can be fetched
                            ProfileModel profile =  api.getProfile();
                            // store profile json
                            if (profile != null ) {
                                PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
                                pref.put(PrefManager.Key.PROFILE_JSON,  profile.json);
                                pref.put(PrefManager.Key.AUTH_TOKEN_BACKEND, null);
                                pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL, null);
                            }
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
