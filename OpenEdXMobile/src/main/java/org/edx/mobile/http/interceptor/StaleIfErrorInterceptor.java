package org.edx.mobile.http.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;

/**
 * An OkHttp interceptor that adds the 'stale-if-error' Cache-Control directive to requests if
 * possible and not already present. The maximum stale value will be set to
 * {@link Integer#MAX_VALUE} in seconds.
 */
public class StaleIfErrorInterceptor implements Interceptor {
    /**
     * A regular expression for finding a valid 'stale-if-error' directive in the Cache-Control
     * header.
     */
    private static final Pattern PATTERN_STALE_IF_ERROR = Pattern.compile(
            "(?:^|[,;])\\s*stale-if-error\\s*(?:=\\s*[^,;\\s]+\\s*)?(?:$|[,;])");

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        Request request = chain.request();
        // Verify that the HTTP method is for loading data only and doesn't have any side-effects,
        // and that the request doesn't contain the 'only-if-cached' Cache-Control directive to
        // force loading from the cache.
        if (!HttpMethod.invalidatesCache(request.method()) &&
                !request.cacheControl().onlyIfCached()) {
            // If the request already has the 'stale-if-error' Cache-Control directive, then proceed
            // the request chain without interference.
            for (final String cacheControlValue : request.headers("Cache-Control")) {
                if (PATTERN_STALE_IF_ERROR.matcher(cacheControlValue).matches()) {
                    return chain.proceed(request);
                }
            }
            // Otherwise add a 'stale-if-error' Cache-Control directive, with the maximum stale
            // value set to a very high value.
            request = request.newBuilder()
                    .addHeader("Cache-Control", "stale-if-error=" + Integer.MAX_VALUE)
                    .build();
        }
        return chain.proceed(request);
    }
}
