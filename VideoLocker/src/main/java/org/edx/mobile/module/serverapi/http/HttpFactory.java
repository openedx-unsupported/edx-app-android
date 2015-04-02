package org.edx.mobile.module.serverapi.http;

/**
 * Created by rohan on 2/6/15.
 */
public class HttpFactory {

    /**
     * Returns a new instance of {@link org.edx.mobile.module.serverapi.http.IHttp} class.
     * @return
     */
    public static IHttp getInstance() {
        return new IHttpImpl();
    }
}
