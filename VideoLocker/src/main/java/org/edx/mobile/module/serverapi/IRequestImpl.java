package org.edx.mobile.module.serverapi;

import android.os.Bundle;

/**
 * Created by rohan on 2/6/15.
 */
class IRequestImpl implements IRequest {

    private String endpoint;
    private Bundle params;
    private Bundle headers;
    private String postBody;

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getPostBody() {
        return postBody;
    }

    @Override
    public void setPostBody(String postBody) {
        this.postBody = postBody;
    }

    @Override
    public void addParameter(String key, String value) {
        if (params == null) {
            params = new Bundle();
        }
        params.putString(key, value);
    }

    @Override
    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new Bundle();
        }
        headers.putString(key, value);
    }

    @Override
    public Bundle getParameters() {
        return params;
    }

    @Override
    public void setParameters(Bundle parameters) {
        this.params = parameters;
    }

    @Override
    public Bundle getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Bundle headers) {
        this.headers = headers;
    }

    public static final class Method {
        public static final String GET      = "GET";
        public static final String POST     = "POST";
        public static final String PUT      = "PUT";
        public static final String DELETE   = "DELETE";
        public static final String PATCH    = "PATCH";
    }
}
