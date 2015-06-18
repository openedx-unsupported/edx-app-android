package org.edx.mobile.http;

import android.content.Context;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.NetworkUtil;

import retrofit.RequestInterceptor;

/**
 * okhttp does not work with offline mode by default.
 * we force okhttp to look at the local cache when in offline mode
 */
public class OfflineRequestInterceptor implements RequestInterceptor {
    protected final Logger logger = new Logger(getClass().getName());

    private Context context;
    public OfflineRequestInterceptor(Context context){
        this.context = context;
    }

    @Override
    public void intercept(RequestInterceptor.RequestFacade request) {
        if (isNetworkConnected()) {
            //TODO? should we specify caching here?
//            int maxAge = 60; // read from cache for 1 minute
//            request.addHeader("Cache-Control", "public, max-age=" + maxAge);
            request.addHeader("Cache-Control", "public");
        } else {
            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            request.addHeader("Cache-Control",
                "public, only-if-cached, max-stale=" + maxStale);
        }
    }

    //TODO - we dont need this method after DI is used
    public boolean isNetworkConnected(){
        return NetworkUtil.isConnected(context);
    }
}
