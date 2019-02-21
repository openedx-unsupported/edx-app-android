package org.edx.mobile.tta.wordpress_client.rest.interceptor;

import org.edx.mobile.tta.wordpress_client.util.LogUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Arjun Singh
 *         Created on 2016/01/14.
 */
public class OkHttpDebugInterceptor implements Interceptor {

    private boolean mShowResponse = false;

    private static final String DEBUG_TAG = "OkHttpRequest";

    public OkHttpDebugInterceptor() {
        this(false);
    }

    public OkHttpDebugInterceptor(boolean showResponse) {
        mShowResponse = showResponse;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        StringBuilder sb = new StringBuilder();

        sb.append("******** [REQUEST START] ********\n")
                .append("** URL : ")
                .append(request.url().toString())
                .append("\n")
                .append("** HTTP Method : ")
                .append(request.method())
                .append("\n");

        Response response = chain.proceed(request);

        sb.append("** RESPONSE : (")
                .append(response.code())
                .append(") ")
                .append(response.message())
                .append("\n");

        // Enabling this stops the callbacks from being able to read the response body because the inputstream gets closed.
        // only really useful to check what responses are to create proper handlers
        if (mShowResponse) {
            if (response.body() != null) {
                sb.append("** BODY : ")
                        .append(response.body().string());
            }
        }

        sb.append("******** [RESPONSE END] ********\n")
                .append("\n");

        LogUtils.d(DEBUG_TAG, sb.toString());

        return response;
    }
}
