package org.edx.mobile.http.interceptor;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.text.Normalizer;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

    @NonNull
    private final String userAgent;

    public UserAgentInterceptor(@NonNull String userAgent) {
        this.userAgent = Normalizer.normalize(userAgent, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build());
    }
}
