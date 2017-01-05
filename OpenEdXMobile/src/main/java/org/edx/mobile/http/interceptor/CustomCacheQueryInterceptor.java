package org.edx.mobile.http.interceptor;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.cache.CacheManager;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import okhttp3.CipherSuite;
import okhttp3.Handshake;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okhttp3.internal.http.CacheStrategy;
import okhttp3.internal.http.HttpEngine;
import okhttp3.internal.http.HttpMethod;
import roboguice.RoboGuice;

/**
 * An OkHttp interceptor that adds support for querying the deprecated {@link CacheManager}
 * initially, if the OkHttp client doesn't have the entry in it's own cache.
 *
 * @deprecated This is only provided so that the transition from the Apache client to OkHttp can be
 * performed smoothly without any user-facing issues. After a significant percentage of the userbase
 * have upgraded to a version that uses the OkHttp API in the Courseware module, this may be removed
 * along with the CacheManager class itself.
 */
@Deprecated
public class CustomCacheQueryInterceptor implements Interceptor {
    /**
     * A dummy TLS handshake to add to the custom-built cache responses for HTTPS requests, in order
     * to have the CacheStrategy resolver validate it.
     */
    private static final Handshake DUMMY_HANDSHAKE = Handshake.get(
            TlsVersion.TLS_1_2,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST);

    @Inject
    private CacheManager cacheManager;

    public CustomCacheQueryInterceptor(@NonNull final Context context) {
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Request request = chain.request();
        Response response = chain.proceed(request);
        final String urlString = request.url().toString();
        // If the OkHttp client
        if (response.cacheResponse() != null) {
            cacheManager.remove(urlString);
        } else {
            final String cachedBody = cacheManager.get(urlString);
            if (cachedBody != null) {
                /* Since we don't cache the metadata, the cached entries should always be assumed to
                 * have a 200 response code. The body is not provided in this response, because it
                 * would only be needed if we don't have a network response.
                 */
                Response cacheResponse = response.newBuilder()
                        .code(HttpStatus.OK)
                        .message("OK")
                        .handshake(request.isHttps() ? DUMMY_HANDSHAKE : null)
                        .priorResponse(null)
                        .networkResponse(null)
                        .cacheResponse(null)
                        .body(null)
                        .build();
                final CacheStrategy cacheStrategy = new CacheStrategy.Factory(
                        System.currentTimeMillis(), request, cacheResponse).get();
                cacheResponse = cacheStrategy.cacheResponse;
                if (cacheResponse != null) {
                    /* Either querying the server is forbidden by the Cache-Control headers (if
                     * there is no network response), or they require a conditional query
                     * (otherwise). In the latter case, either the server has validated the cached
                     * response or not. Only in the last case would the network response be
                     * delivered; in the first two cases the cached response would be delivered
                     * instead.
                     */
                    final Response networkResponse = response.networkResponse();
                    if (networkResponse == null ||
                            shouldUseCachedResponse(cacheResponse, networkResponse)) {
                        response = response.newBuilder()
                                .code(HttpStatus.OK)
                                .cacheResponse(cacheResponse)
                                .body(ResponseBody.create(MediaType.parse("application/json"),
                                        cachedBody))
                                .build();
                    } else {
                        response = response.newBuilder()
                                .cacheResponse(cacheResponse)
                                .build();
                        if (HttpEngine.hasBody(response) &&
                                HttpMethod.invalidatesCache(request.method())) {
                            cacheManager.remove(urlString);
                        }
                    }
                }
            }
        }
        return response;
    }

    /**
     * Check whether the cached or network response is more suitable to return to the user agent,
     * and return the result.
     *
     * @param cachedResponse The cached response.
     * @param networkResponse The network response.
     * @return True if {@code cachedResponse} should be used; false if {@code networkResponse}
     * should be used.
     */
    private static boolean shouldUseCachedResponse(@NonNull final Response cachedResponse,
                                                   @NonNull final Response networkResponse) {
        if (networkResponse.code() == HttpStatus.NOT_MODIFIED) {
            return true;
        }

        /* The HTTP spec says that if the network's response is older than our cached response, we
         * may return the cache's response. Like Chrome (but unlike Firefox), the OkHttp client
         * prefers to return the newer response.
         */
        final Date lastModified = cachedResponse.headers().getDate("Last-Modified");
        if (lastModified != null) {
            final Date networkLastModified = networkResponse.headers().getDate("Last-Modified");
            if (networkLastModified != null
                    && networkLastModified.getTime() < lastModified.getTime()) {
                return true;
            }
        }

        return false;
    }
}
