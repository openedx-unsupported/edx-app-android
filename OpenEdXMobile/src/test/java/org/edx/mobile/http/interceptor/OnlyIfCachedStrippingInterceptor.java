package org.edx.mobile.http.interceptor;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OnlyIfCachedStrippingInterceptor implements Interceptor {
    private static final Pattern PATTERN_ONLY_IF_CACHED_HEADER = Pattern.compile(
            "(^|[,;])\\s*?only-if-cached\\s*?($|[,;])", Pattern.CASE_INSENSITIVE);
    private static final int GROUP_SEPARATOR_START = 1;
    private static final int GROUP_SEPARATOR_END = 2;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (request.cacheControl().onlyIfCached()) {
            final Headers headers = request.headers();
            final Headers.Builder strippedHeadersBuilder = new Headers.Builder();
            for (int i = 0, headersCount = headers.size(); i < headersCount; i++) {
                final String headerName = headers.name(i);
                String headerValue = headers.value(i);
                if (headerName.equalsIgnoreCase("Cache-Control")) {
                    Matcher directiveMatcher = PATTERN_ONLY_IF_CACHED_HEADER.matcher(headerValue);
                    if (directiveMatcher.find()) {
                        while (true) {
                            final StringBuffer newHeaderValueBuffer = new StringBuffer();
                            directiveMatcher.appendReplacement(newHeaderValueBuffer, "$" +
                                    (directiveMatcher.group(GROUP_SEPARATOR_START).isEmpty() ?
                                            GROUP_SEPARATOR_END : GROUP_SEPARATOR_START));
                            directiveMatcher.appendTail(newHeaderValueBuffer);
                            headerValue = newHeaderValueBuffer.toString();
                            directiveMatcher = PATTERN_ONLY_IF_CACHED_HEADER
                                    .matcher(headerValue);
                            if (!directiveMatcher.find()) break;
                        }
                        if (headerName.isEmpty()) continue;
                    }
                }
                strippedHeadersBuilder.add(headerName, headerValue);
            }
            request = request.newBuilder()
                    .headers(strippedHeadersBuilder.build())
                    .build();
        }
        return chain.proceed(request);
    }
}
