package org.edx.mobile.tta.analytics;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.tta.wordpress_client.rest.interceptor.OkHttpBearerTokenAuthInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.edx.mobile.util.BrowserUtil.loginPrefs;


/**
 * Created by mukesh on 24/4/18.
 */

public interface AnalyticsRetrofitProvider extends Provider<AnalyticsRetrofitProvider> {
    @NonNull
    public Retrofit getAnalyticsRetrofit();

    class Impl implements AnalyticsRetrofitProvider{

        @Inject
        EdxEnvironment mEnv;

        @Inject OkHttpClient okHttpClient;
        @Inject
        Gson gson;
        private Retrofit retrofit =null;
        @Override
        public AnalyticsRetrofitProvider get() {
            return new Impl();
        }

        @NonNull
        @Override
        public Retrofit getAnalyticsRetrofit() {
            //if (retrofit== null) {
                retrofit= new Retrofit.Builder()
                        .client(okHttpClient)
                        .baseUrl(mEnv.getConfig().getAnalyticsStoreUrl())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
          //  }
            return retrofit;
        }
        private OkHttpClient getClient() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.addInterceptor(new OkHttpBearerTokenAuthInterceptor(loginPrefs.getCurrentAuth()==null?"":loginPrefs.getCurrentAuth().access_token));
            return builder.build();
        }
    }
}
