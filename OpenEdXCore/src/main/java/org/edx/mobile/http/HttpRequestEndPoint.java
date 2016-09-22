package org.edx.mobile.http;

import java.util.Map;

/**
 * Created by hanning on 5/30/15.
 */
public interface HttpRequestEndPoint {
    String getUrl();
    String getCacheKey();
    Map<String,String> getParameters();
}
