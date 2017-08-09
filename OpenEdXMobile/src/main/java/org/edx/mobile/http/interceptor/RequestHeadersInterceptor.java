package org.edx.mobile.http.interceptor;

import android.support.annotation.NonNull;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.util.LocaleUtils;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * {@link Interceptor} to modify request headers.
 */
public class RequestHeadersInterceptor implements Interceptor {
    @NonNull
    private final String userAgent;

    public RequestHeadersInterceptor() {
        this.userAgent = System.getProperty("http.agent") + " " +
                MainApplication.instance().getString(R.string.app_name) + "/" +
                BuildConfig.APPLICATION_ID + "/" +
                BuildConfig.VERSION_NAME;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .header("Accept-Language", LocaleUtils.getLanguageCodeFromLocale(Locale.getDefault()))
                .build());
    }
}
