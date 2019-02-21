package org.edx.mobile.tta.wordpress_client.rest.interceptor;

import android.util.Base64;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Arjun Singh
 *         Created on 2016/01/14.
 */
public class OkHttpBasicAuthInterceptor implements Interceptor {

    private String mUser;
    private String mPass;

    public OkHttpBasicAuthInterceptor(String user, String pass) {
        mUser = user;
        mPass = pass;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final byte[] encodedAuth = Base64.encode((mUser + ":" + mPass).getBytes(), Base64.NO_WRAP);
        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Basic " + new String(encodedAuth))
                .build();
        return chain.proceed(request);
    }
}
