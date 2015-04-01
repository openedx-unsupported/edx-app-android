package org.edx.mobile.task;

import android.content.Context;
import android.os.Bundle;

import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.SocialLoginResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;
import org.edx.mobile.social.SocialFactory;


public abstract class RegisterTask extends Task<RegisterResponse> {

    private Bundle parameters;
    private SocialFactory.SOCIAL_SOURCE_TYPE backstoreType;
    private String accessToken;
    private AuthResponse authResponse;
    private SocialLoginResponse socialLoginResponse;

    public RegisterTask(Context context, Bundle parameters, String accessToken, SocialFactory.SOCIAL_SOURCE_TYPE backstoreType) {
        super(context);
        this.parameters = parameters;
        this.accessToken = accessToken;
        this.backstoreType = backstoreType;
    }

    @Override
    protected RegisterResponse doInBackground(Object... params) {
        try {
            IApi api = ApiFactory.getCacheApiInstance(context);
            RegisterResponse res = api.doRegister(parameters);

            if (res.isSuccess()) {
                switch ( backstoreType ){
                    case  TYPE_GOOGLE :
                    case  TYPE_FACEBOOK :

                         // do SOCIAL LOGIN first
                        if ( backstoreType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_FACEBOOK ) {
                            socialLoginResponse = api.doLoginByFacebook(accessToken);
                        } else if ( backstoreType == SocialFactory.SOCIAL_SOURCE_TYPE.TYPE_GOOGLE ) {
                            socialLoginResponse = api.doLoginByGoogle(accessToken);
                        }
                        if (socialLoginResponse != null && socialLoginResponse.isSuccess()) {
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

                        authResponse = LoginTask.getAuthResponse(context, username, password);
                        if (authResponse.isSuccess()) {
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

    public AuthResponse getAuthResponse() {
        return authResponse;
    }

    public SocialLoginResponse getSocialLoginResponse() {
        return socialLoginResponse;
    }
}
