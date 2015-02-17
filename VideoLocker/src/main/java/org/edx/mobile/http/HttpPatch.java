package org.edx.mobile.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

/**
 * This class represents HTTP PATCH request.
 * Instead of {@link HttpPost}, this class can be used to make PATCH requests using {@link HttpClient}.
 * @author rohan
 *
 */
public class HttpPatch extends HttpPost {
    
    public static final String METHOD_PATCH = "PATCH";

    public HttpPatch(final String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return METHOD_PATCH;
    }
}