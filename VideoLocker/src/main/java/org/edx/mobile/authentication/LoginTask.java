package org.edx.mobile.authentication;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.task.Task;

public abstract class LoginTask extends Task<AuthResponse>  {

    @NonNull
    private final String username;
    @NonNull
    private final String password;

    @Inject
    private LoginAPI loginAPI;

    public LoginTask(@NonNull Context context, @NonNull String username, @NonNull String password) {
        super(context);
        this.username = username;
        this.password = password;
    }

    @Override
    public AuthResponse call() throws Exception {
        AuthResponse response;
        try {
            response = loginAPI.getAccessToken(username, password); }
        catch (RetroHttpException exception) {
            return null;
        }

        // store auth token response
        Gson gson = new GsonBuilder().create();
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_JSON, gson.toJson(response));
        pref.put(PrefManager.Key.SEGMENT_KEY_BACKEND, ISegment.Values.PASSWORD);

        ServiceManager api = environment.getServiceManager();

        // get profile of this user
        if (response.isSuccess()) {
            response.profile = api.getProfile();

        }
        return response;
    }
}
