package org.edx.mobile.discovery;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.edx.mobile.core.EdxEnvironment;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public interface DiscoveryRetrofitProvider extends Provider<DiscoveryRetrofitProvider> {
    @NonNull
    public Retrofit getDiscoveryBaseRetrofit();
    @Singleton
    class Impl implements DiscoveryRetrofitProvider {

        @Inject
        EdxEnvironment mEnv;

        @Inject
        Gson gson;
        private Retrofit retrofit =null;

        @NonNull
        @Override
        public Retrofit getDiscoveryBaseRetrofit() {
            if (retrofit == null) {
                retrofit= new Retrofit.Builder()
                        .client(getClient())
                        .baseUrl(mEnv.getConfig().getDiscoveryBaseUrl())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
            return retrofit;
        }


        private OkHttpClient getClient() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            builder.addInterceptor(new AuthTokenInterceptor(mEnv.getLoginPrefs().getCurrentAuth().access_token));
            builder.addInterceptor(loggingInterceptor);
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    return chain.proceed(request);
                }
            });
            builder.cache(null);
            builder.connectTimeout(100, TimeUnit.SECONDS)
                    .writeTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS);
            return builder.build();
        }

        @Override
        public DiscoveryRetrofitProvider get() {
            return new Impl();
        }
    }
}

