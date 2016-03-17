package org.edx.mobile.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.services.ServiceManager;

/**
 * This task represents Task for login by edX username and password.
 * @author rohan
 *
 */
public abstract class LoginTask extends Task<AuthResponse> {
    @NonNull
    String username;
    @NonNull
    String password;

    public LoginTask(@NonNull Context context, @NonNull String username, @NonNull String password) {
        super(context);
        this.username = username;
        this.password = password;
    }

    @Override
    public AuthResponse call() throws Exception{
        return getAuthResponse(context, username, password);
    }

    public  AuthResponse getAuthResponse(Context context, String username, String password) throws Exception {
        ServiceManager api = environment.getServiceManager();
        AuthResponse res = api.auth(username, password);

        // get profile of this user
        if (res.isSuccess()) {
            res.profile = api.getProfile();

        }
        return res;
    }
}
