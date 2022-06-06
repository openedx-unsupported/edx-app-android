package org.edx.mobile.base.http.interceptor;

import android.content.Context;

import androidx.annotation.NonNull;

import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;

import java.io.IOException;

import dagger.hilt.android.EntryPointAccessors;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Injects OAuth token - if present - into Authorization header
 **/
public final class OauthHeaderRequestInterceptor implements Interceptor {
    protected final Logger logger = new Logger(getClass().getName());

    private final LoginPrefs loginPrefs;

    public OauthHeaderRequestInterceptor(Context context) {
        loginPrefs = EntryPointAccessors
                .fromApplication(context, EdxDefaultModule.ProviderEntryPoint.class).getLoginPrefs();
    }

    @Override
    @NonNull
    public Response intercept(Chain chain) throws IOException {
        final Request.Builder builder = chain.request().newBuilder();
        final String token = loginPrefs.getAuthorizationHeader();
        if (token != null) {
            builder.addHeader("Authorization", token);
        }
        return chain.proceed(builder.build());
    }

}
