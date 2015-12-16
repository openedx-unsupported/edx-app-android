package org.edx.mobile.http;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.apache.http.cookie.Cookie;
import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.Date;
import java.util.List;

public class OkHttpUtil {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    private static final int cacheSize = 10 * 1024 * 1024; // 10 MiB

    public static OkHttpClient getClient(@NonNull Context context) {
        return getClient(context, false);
    }

    public static OkHttpClient getOAuthBasedClient(@NonNull Context context) {
        return getClient(context, true);
    }

    private static OkHttpClient getClient(@NonNull Context context, boolean isOAuthBased) {
        final OkHttpClient client = new OkHttpClient();
        final File cacheDirectory = new File(context.getFilesDir(), "http-cache");
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        final Cache cache = new Cache(cacheDirectory, cacheSize);
        client.setCache(cache);
        List<Interceptor> interceptors = client.interceptors();
        interceptors.add(new JsonMergePatchInterceptor());
        interceptors.add(new UserAgentInterceptor(
                System.getProperty("http.agent") + " " +
                        context.getString(R.string.app_name) + "/" +
                        BuildConfig.APPLICATION_ID + "/" +
                        BuildConfig.VERSION_NAME));
        if (isOAuthBased) {
            interceptors.add(new OauthHeaderRequestInterceptor(context));
        }
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            interceptors.add(loggingInterceptor);
        }
        return client;
    }

    /**
     * Sets cookie headers like "X-CSRFToken" in the given bundle.
     * This method is helpful in making API calls the way website does.
     *
     * @return
     * @throws Exception
     */
    public static Bundle setCookieHeaders(Response response, Bundle headerBundle) throws Exception {
        Headers headers = response.headers();

        for (int i = 0; i < headers.size(); i++) {
            if (headers.name(i).equalsIgnoreCase("csrftoken")) {
                headerBundle.putString("Cookie", headers.name(i)
                        + "=" + headers.value(i));
                headerBundle.putString("X-CSRFToken", headers.value(i));
                break;
            }
        }

        return headerBundle;
    }

    /**
     * add fields to request header
     */
    public static void addHeader(Request.Builder builder, Bundle bundle) {
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                builder.addHeader(key, bundle.getString(key));
            }
        }
    }

    /**
     * add fileds to request header.
     */
    public static Response addHeader(Interceptor.Chain chain, Bundle bundle) throws IOException {
        Request originalRequest = chain.request();

        Request.Builder builder = originalRequest.newBuilder();
        addHeader(builder, bundle);
        return chain.proceed(builder.build());
    }

    /**
     * get cookie for request. [GET or POST]
     */
    public static List<HttpCookie> getCookies(Context context, String url, boolean isGet)
            throws Exception {

        OkHttpClient oauthBasedClient = getOAuthBasedClient(context);
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        oauthBasedClient.setCookieHandler(cookieManager);

        Request.Builder builder = new Request.Builder();
        if (!isGet) {
            RequestBody body = RequestBody.create(JSON, "");
            builder.post(body);
        }
        Request request = builder.url(url).build();
        Response response = oauthBasedClient.newCall(request).execute();

        CookieStore cookieStore = cookieManager.getCookieStore();
        return cookieStore.getCookies();
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
    public static HttpCookie servletCookieFromApacheCookie(Cookie apacheCookie) {
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
