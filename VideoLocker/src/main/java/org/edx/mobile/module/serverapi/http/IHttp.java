package org.edx.mobile.module.serverapi.http;

import org.edx.mobile.module.serverapi.IRequest;
import org.edx.mobile.module.serverapi.IResponse;

import java.io.IOException;

/**
 * Created by rohan on 2/6/15.
 */
public interface IHttp {

    /* Keys for Response headers */
    static final String KEY_X_CSRFTOKEN     = "X-CSRFToken";
    static final String KEY_COOKIE          = "Cookie";

    IResponse get(IRequest request) throws IOException;
    IResponse post(IRequest request) throws IOException;
    IResponse patch(IRequest request) throws IOException;
}
