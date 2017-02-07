package org.edx.mobile.http;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;

import java.io.File;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpUtil {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    private static final int cacheSize = 10 * 1024 * 1024; // 10 MiB

    public static OkHttpClient getClient(@NonNull Context context) {
        return getClient(context, false, false);
    }

    public static OkHttpClient getOAuthBasedClient(@NonNull Context context) {
        return getClient(context, true, false);
    }

    public static OkHttpClient getOAuthBasedClientWithOfflineCache(@NonNull Context context) {
        return getClient(context, true, true);
    }

    private static OkHttpClient getClient(@NonNull Context context,
                                          boolean isOAuthBased, boolean usesOfflineCache) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        List<Interceptor> interceptors = builder.interceptors();
        if (usesOfflineCache) {
            final File cacheDirectory = new File(context.getFilesDir(), "http-cache");
            if (!cacheDirectory.exists()) {
                cacheDirectory.mkdirs();
            }
            final Cache cache = new Cache(cacheDirectory, cacheSize);
            builder.cache(cache);
            interceptors.add(new OfflineRequestInterceptor(context));
        }
        interceptors.add(new JsonMergePatchInterceptor());
        interceptors.add(new UserAgentInterceptor(
                System.getProperty("http.agent") + " " +
                        context.getString(R.string.app_name) + "/" +
                        BuildConfig.APPLICATION_ID + "/" +
                        BuildConfig.VERSION_NAME));
        if (isOAuthBased) {
            interceptors.add(new OauthHeaderRequestInterceptor(context));
        }
        interceptors.add(new NewVersionBroadcastInterceptor());
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            interceptors.add(loggingInterceptor);
        }

        builder.authenticator(new OauthRefreshTokenAuthenticator(context));
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.connectTimeout(30, TimeUnit.SECONDS);

        return builder.build();
    }

    /**
     * get cookie for request. [GET or POST]
     */
    public static List<HttpCookie> getCookies(Context context, String url, boolean isGet)
            throws Exception {
        final List<HttpCookie> cookies = new ArrayList<>();

        OkHttpClient.Builder oauthBasedClientBuilder = getOAuthBasedClient(context).newBuilder();
        oauthBasedClientBuilder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> newCookies) {
                for (Cookie cookie : newCookies) {
                    cookies.addAll(HttpCookie.parse(cookie.toString()));
                }
            }

            @Override
            public List<okhttp3.Cookie> loadForRequest(HttpUrl url) {
                return null;
            }
        });
        OkHttpClient oauthBasedClient = oauthBasedClientBuilder.build();

        Request.Builder builder = new Request.Builder();
        if (!isGet) {
            RequestBody body = RequestBody.create(JSON, "");
            builder.post(body);
        }
        Request request = builder.url(url).build();
        Response response = oauthBasedClient.newCall(request).execute();

        return cookies;
    }

    /**
     * Returns GET url with appended parameters.
     *
     * @param url
     * @param params
     * @return
     */
    public static String toGetUrl(String url, Bundle params) {
        if (params != null) {
            if (!url.endsWith("?")) {
                url = url + "?";
            }

            for (String key : params.keySet()) {
                url = url + key + "=" + params.getString(key) + "&";
            }
        }
        return url;
    }

    //http://sangupta.com/tech/convert-between-java-servlet-and-apache.html
    @Deprecated // Deprecated because this uses org.apache.http, which is itself deprecated
    public static HttpCookie servletCookieFromApacheCookie(
            org.apache.http.cookie.Cookie apacheCookie) {
        if (apacheCookie == null) {
            return null;
        }

        String name = apacheCookie.getName();
        String value = apacheCookie.getValue();

        HttpCookie cookie = new HttpCookie(name, value);

        value = apacheCookie.getDomain();
        if (value != null) {
            cookie.setDomain(value);
        }
        value = apacheCookie.getPath();
        if (value != null) {
            cookie.setPath(value);
        }
        cookie.setSecure(apacheCookie.isSecure());

        value = apacheCookie.getComment();
        if (value != null) {
            cookie.setComment(value);
        }

        // version
        cookie.setVersion(apacheCookie.getVersion());

        // From the Apache source code, maxAge is converted to expiry date using the following formula
        // if (maxAge >= 0) {
        //     setExpiryDate(new Date(System.currentTimeMillis() + maxAge * 1000L));
        // }
        // Reverse this to get the actual max age

        Date expiryDate = apacheCookie.getExpiryDate();
        if (expiryDate != null) {
            long maxAge = (expiryDate.getTime() - System.currentTimeMillis()) / 1000;
            // we have to lower down, no other option
            cookie.setMaxAge((int) maxAge);
        }

        // return the servlet cookie
        return cookie;
    }

    public static enum REQUEST_CACHE_TYPE {IGNORE_CACHE, PREFER_CACHE, ONLY_CACHE}
}
