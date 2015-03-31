package org.edx.mobile.module.serverapi;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.serverapi.retrofit.IRestApi;
import org.edx.mobile.util.Config;

import java.io.File;
import java.io.IOException;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by rohan on 2/7/15.
 */
public class ApiFactory {

    private static final Logger logger = new Logger(ApiFactory.class);
    private static IRestApi restApi = null;
    private static IApi cacheApi = null;

    /**
     * Returns new instance of {@link org.edx.mobile.module.serverapi.retrofit.IRestApi} class.
     * @param context
     * @return
     */
    public static IRestApi getRestApiInstance(Context context) {
        if (restApi == null) {
            OkHttpClient client = new OkHttpClient();

            try {
                // setup cache

                int cacheSize = 10 * 1024 * 1024; // 10 MiB
                File cacheDirectory = new File(context.getCacheDir().getAbsolutePath(), "HttpCache");
                Cache cache = new Cache(cacheDirectory, cacheSize);
                client.setCache(cache);
            } catch (IOException e) {
                logger.error(e);
            }

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(Config.getInstance().getApiHostURL())
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setClient(new OkClient(client))
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            // update cache after every 10 minutes
                            int maxAge = 10 * 60;
                            request.addHeader("Cache-Control", "public, max-age=" + maxAge);
                        }
                    })
                    .build();

            restApi = restAdapter.create(IRestApi.class);
        }

        return restApi;
    }

    public static IApi getCacheApiInstance(Context context) {
        if (cacheApi == null) {
            cacheApi = new IApiImpl(context);
        }

        return cacheApi;
    }
}
