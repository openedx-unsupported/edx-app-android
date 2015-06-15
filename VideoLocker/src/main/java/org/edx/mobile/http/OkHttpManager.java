package org.edx.mobile.http;

import com.squareup.okhttp.OkHttpClient;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * It is recommended to use singleton for OkHttpClient
 */
public class OkHttpManager {

    private static OkHttpManager instance;

    public static final synchronized  OkHttpManager getInstance(){
        if ( instance == null )
            instance = new OkHttpManager();
        return instance;
    }
    private final int cacheSize = 10 * 1024 * 1024; // 10 MiB
    private final OkHttpClient client;
    private  com.squareup.okhttp.Cache cache;

    private OkHttpManager(){
        client = new OkHttpClient();
        File cacheDirectory = new File(MainApplication.instance().getFilesDir(), "http-cache");
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        cache = new com.squareup.okhttp.Cache(cacheDirectory, cacheSize);
        client.setCache(cache);
        int timeoutMillis = MainApplication.instance().getResources().getInteger(R.integer.speed_test_timeout_in_milliseconds);
        client.setConnectTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        client.interceptors().add(new GzipRequestInterceptor());
    }

    public final OkHttpClient getClient(){
        return client;
    }


}
