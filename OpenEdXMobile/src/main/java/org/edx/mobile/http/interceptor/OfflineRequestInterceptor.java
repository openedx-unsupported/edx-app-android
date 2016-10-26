package org.edx.mobile.http.interceptor;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An OkHttp interceptor that ensures that any queries which
 * couldn't connect to the server would get a response served
 * from the local cache instead.
 */
public class OfflineRequestInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();
        try {
            //TODO? should we specify caching here?
//            requestBuilder.cacheControl(new CacheControl.Builder()
//                    .maxAge(1, TimeUnit.MINUTES).build()); // read from cache for 1 minute
            return chain.proceed(request);
        } catch (IOException e) {
            return chain.proceed(request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE).build());
        }
    }
}
