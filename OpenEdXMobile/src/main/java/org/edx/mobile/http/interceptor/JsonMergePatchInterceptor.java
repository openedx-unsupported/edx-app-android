package org.edx.mobile.http.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Interceptor for setting the JSON Merge Patch content type
 */
public class JsonMergePatchInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (request.method().equals("PATCH")) {
            MediaType mediaType = request.body().contentType();
            if (mediaType.type().equalsIgnoreCase("application") &&
                    mediaType.subtype().equalsIgnoreCase("json")) {
                request = request.newBuilder()
                        .method(request.method(),
                                new RequestBodyWrapper(request.body()))
                        .build();
            }
        }
        return chain.proceed(request);
    }

    /**
     * Wrapper around the request body that changes the content
     * type to the JSON Merge Patch format.
     */
    private static class RequestBodyWrapper extends RequestBody {
        private static final MediaType MEDIA_TYPE_JSON_MERGE_PATCH =
                MediaType.parse("application/merge-patch+json");

        private final RequestBody body;

        RequestBodyWrapper(RequestBody body) {
            this.body = body;
        }

        @Override
        public MediaType contentType() {
            return MEDIA_TYPE_JSON_MERGE_PATCH;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            body.writeTo(sink);
        }

        @Override
        public long contentLength() throws IOException {
            return body.contentLength();
        }
    }
}
