package org.edx.mobile.http;

import android.content.Context;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.NetworkUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * okhttp does not work with offline mode by default.
 * we force okhttp to look at the local cache when in offline mode
 */
public class OfflineRequestInterceptor implements Interceptor {
    protected final Logger logger = new Logger(getClass().getName());
    private final static int MAX_STALE = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
    private Context context;
    private int maxStaleTime;
    public OfflineRequestInterceptor(Context context){
        this(context, MAX_STALE);
    }

    public OfflineRequestInterceptor(Context context, int maxStaleTime){

        this.context = context;
        this.maxStaleTime = maxStaleTime;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request.Builder requestBuilder = chain.request().newBuilder();
        if (isNetworkConnected()) {
            //TODO? should we specify caching here?
//            int maxAge = 60; // read from cache for 1 minute
//            requestBuilder.header(("Cache-Control", "public, max-age=" + maxAge);
            requestBuilder.header("Cache-Control", "public");
        } else {
            requestBuilder.header("Cache-Control",
                    "public, only-if-cached, max-stale=" + maxStaleTime);
        }
        return chain.proceed(requestBuilder.build());
    }

    //TODO - we dont need this method after DI is used
    public boolean isNetworkConnected(){
        return NetworkUtil.isConnected(context);
    }
}
