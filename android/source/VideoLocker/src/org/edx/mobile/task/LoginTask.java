package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.module.prefs.PrefManager;

/**
 * This task represents Task for login by edX username and password.
 * @author rohan
 *
 */
public abstract class LoginTask extends Task<AuthResponse> {

    public LoginTask(Context context) {
        super(context);
    }

    @Override
    protected AuthResponse doInBackground(Object... params) {
        try { 
            String username = params[0].toString();
            String password = params[1].toString();
            if(username!=null) {
                return getAuthResponse(context, username, password);
            }
        } catch(Exception ex) {
            handle(ex);
            logger.error(ex);
        }
        return null;
    }

    public static AuthResponse getAuthResponse(Context context, String username, String password) throws Exception {
        Api api = new Api(context);
        AuthResponse res = api.auth(username, password);

        // get profile of this user
        if (res.isSuccess()) {
            res.profile = api.getProfile();

            // store profile json
            if (res.profile != null) {
                PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
                pref.put(PrefManager.Key.PROFILE_JSON, res.profile.json);
                pref.put(PrefManager.Key.AUTH_TOKEN_BACKEND, null);
                pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL, null);
            }
        }
        return res;
    }
}
