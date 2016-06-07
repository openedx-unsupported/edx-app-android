package org.edx.mobile.http;

import android.content.Context;
import android.support.annotation.NonNull;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import roboguice.RoboGuice;

/**
 *  this interceptor inject oauth token to request header
 **/
public final class OauthHeaderRequestInterceptor implements Interceptor {
    protected final Logger logger = new Logger(getClass().getName());
    @NonNull
    private final Context context;

    @NonNull
    private final LoginPrefs loginPrefs;

    public OauthHeaderRequestInterceptor(@NonNull Context context){
        this.context = context;
        loginPrefs = RoboGuice.getInjector(context).getInstance(LoginPrefs.class);
    }
    @Override public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request.Builder builder = originalRequest.newBuilder();
        final String token = loginPrefs.getAuthorizationHeader();
        if (token != null) {
            builder.addHeader("Authorization", token);
        } else {
            logger.warn("Token cannot be null when AUTH_JSON is also null, something is WRONG!");
        }
        return chain.proceed(builder.build());
    }

}