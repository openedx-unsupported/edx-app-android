package org.edx.mobile.module.serverapi;

import android.os.Bundle;

/**
 * Created by rohan on 2/6/15.
 */
public interface IRequest {

    void setEndpoint(String endpoint);
    String getEndpoint();

    void setPostBody(String postBody);
    String getPostBody();

    void addParameter(String key, String value);
    Bundle getParameters();
    void setParameters(Bundle parameters);

    void addHeader(String key, String value);
    Bundle getHeaders();
    void setHeaders(Bundle headers);
}
