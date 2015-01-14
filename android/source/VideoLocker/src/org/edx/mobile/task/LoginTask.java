package org.edx.mobile.task;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.module.prefs.PrefManager;

import android.content.Context;

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
            if(username!=null){
                Api api = new Api(context);
                AuthResponse res = api.auth(username, params[1].toString());

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
        } catch(Exception ex) {
            handle(ex);
        }
        return null;
    }
}
