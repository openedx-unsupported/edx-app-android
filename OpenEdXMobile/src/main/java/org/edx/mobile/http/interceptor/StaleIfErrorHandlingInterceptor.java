package org.edx.mobile.http.interceptor;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static org.edx.mobile.http.HttpStatus.*;

/**
 * An OkHttp interceptor that adds support for handling the stale-if-error Cache-Control directive,
 * to serve from the local cache upon encountering a server or network error. While the RFC
 * instructions apply only to server errors, the actual usage here includes handling cases where the
 * server couldn't be reached (due to the app being offline, or for other reasons).
 */
public class StaleIfErrorHandlingInterceptor implements Interceptor {
    /**
     * A regular expression for finding a valid 'stale-if-error' directive in the Cache-Control
     * header.
     */
    private static final Pattern PATTERN_STALE_IF_ERROR = Pattern.compile(
            "((?:^|[,;])\\s*?)stale-if-error(\\s*?(=\\s*?[^,;\\s]+\\s*?)?)",
            Pattern.CASE_INSENSITIVE);
    /**
     * A regular expression replacement for
     * {@link StaleIfErrorHandlingInterceptor#PATTERN_STALE_IF_ERROR} that replaces the
     * 'stale-if-error' directive with 'only-if-cached'.
     */
    private static final String REPLACEMENT_FORCE_CACHE = "$1only-if-cached$2";
    /**
     * A regular expression replacement for
     * {@link StaleIfErrorHandlingInterceptor#PATTERN_STALE_IF_ERROR} that replaces the
     * 'stale-if-error=' directive with 'only-if-cached; max-stale='.
     */
    private static final String REPLACEMENT_FORCE_CACHE_MAX_STALE = "$1only-if-cached; max-stale$2";
    /**
     * The capturing group index for the value of the 'stale-if-error' directive in
     * {@link StaleIfErrorHandlingInterceptor#PATTERN_STALE_IF_ERROR}.
     */
    private static final int GROUP_STALE_IF_ERROR_VALUE = 3;

    @NonNull
    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Request request = chain.request();

        // Check to see if the stale-if-error Cache-Control directive is present; if not, then
        // proceed the request chain without interference.
        boolean hasStaleIfErrorDirective = false;
        for (final String cacheControlDirectives : request.headers("Cache-Control")) {
            if (PATTERN_STALE_IF_ERROR.matcher(cacheControlDirectives).matches()) {
                hasStaleIfErrorDirective = true;
                break;
            }
        }
        if (!hasStaleIfErrorDirective) {
            return chain.proceed(request);
        }

        Response response = null;
        IOException error = null;
        try {
            response = chain.proceed(request);
            if (response.cacheResponse() == null) {
                // If there is no cached response, then just return whatever we got.
                return response;
            }
            switch (response.code()) {
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                case GATEWAY_TIMEOUT:
                    // If there is an internal server error, then break in order to deliver the
                    // response from the cache.
                    break;
                default:
                    return response;
            }
        } catch (IOException e) {
            // If a network exception was encountered, store it and fall back to querying the cache
            // for an appropriate response. If none is available from the cache, then throw it.
            error = e;
        }

        // Replace the 'stale-if-error=' directives with 'only-if-cached; max-stale=', in order to
        // force the response to be delivered from the cache.
        final Headers headers = request.headers();
        final Headers.Builder forceCacheHeadersBuilder = new Headers.Builder();
        for (int i = 0, headersCount = headers.size(); i < headersCount; i++) {
            final String headerName = headers.name(i);
            String headerValue = headers.value(i);
            if (headerName.equalsIgnoreCase("Cache-Control")) {
                final Matcher directiveMatcher = PATTERN_STALE_IF_ERROR.matcher(headerValue);
                if (directiveMatcher.find()) {
                    final StringBuffer newHeaderValueBuffer = new StringBuffer();
                    do {
                        /* Verify that the directive is ended properly by the matcher either
                         * reaching the end of the header value string, or a comma or semicolon
                         * separator. Otherwise this directive isn't validly constructed, it will be
                         * skipped.
                         */
                        final int nextCharIndex = directiveMatcher.end();
                        if (nextCharIndex != headerValue.length()) {
                            final char nextChar = headerValue.charAt(nextCharIndex);
                            if (nextChar != ',' && nextChar != ';') {
                                continue;
                            }
                        }
                        directiveMatcher.appendReplacement(newHeaderValueBuffer,
                                directiveMatcher.group(GROUP_STALE_IF_ERROR_VALUE) == null ?
                                        REPLACEMENT_FORCE_CACHE :
                                        REPLACEMENT_FORCE_CACHE_MAX_STALE);
                    } while (directiveMatcher.find());
                    directiveMatcher.appendTail(newHeaderValueBuffer);
                    headerValue = newHeaderValueBuffer.toString();
                }
            }
            forceCacheHeadersBuilder.add(headerName, headerValue);
        }
        final Response newResponse = chain.proceed(
                request.newBuilder()
                        .headers(forceCacheHeadersBuilder.build())
                        .build());
        if (newResponse.code() != GATEWAY_TIMEOUT) {
            return newResponse;
        }

        // The response isn't available in the cache. If a network error was encountered, throw the
        // exception; otherwise return the error response received from the server.
        if (error != null) {
            throw error;
        }
        return response;
    }
}
