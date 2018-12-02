package org.edx.mobile.tta.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by Arjun on 2018/9/18.
 */
public class RetrofitServiceUtil {

    public static IRemoteDataSource create() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //BuildConfig.DEBUG
        if (true) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        OkHttpClient client = builder.addInterceptor(chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader("x-zhsq-code", "zhsq-u3254p-app")
                .addHeader("x-zhsq-app", "user");
                // add your header here

            return chain.proceed(requestBuilder.build());
        })
            .connectTimeout(IRemoteDataSource.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(IRemoteDataSource.READ_TIMEOUT, TimeUnit.SECONDS)
            //.addNetworkInterceptor(new StethoInterceptor())
            .build();

        Gson gson = new GsonBuilder().serializeNulls().create();

        return new Retrofit.Builder()
            .client(client)
            .baseUrl(IRemoteDataSource.BASE_URL)
            //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(IRemoteDataSource.class);
    }
}
