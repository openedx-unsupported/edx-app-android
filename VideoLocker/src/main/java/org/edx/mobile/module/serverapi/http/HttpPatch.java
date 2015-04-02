package org.edx.mobile.module.serverapi.http;

import org.apache.http.client.methods.HttpPost;
import org.edx.mobile.module.serverapi.IRequest;

/**
 * This class represents HTTP PATCH request.
 * Instead of {@link org.apache.http.client.methods.HttpPost}, this class can be used to make PATCH requests using {@link org.apache.http.client.HttpClient}.
 * @author rohan
 *
 */
class HttpPatch extends HttpPost {
    
    public HttpPatch(final String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return IRequest.Method.PATCH;
    }
}