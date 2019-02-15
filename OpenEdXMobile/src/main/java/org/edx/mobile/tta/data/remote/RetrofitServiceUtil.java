package org.edx.mobile.tta.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.http.authenticator.OauthRefreshTokenAuthenticator;
import org.edx.mobile.http.interceptor.CustomCacheQueryInterceptor;
import org.edx.mobile.http.interceptor.JsonMergePatchInterceptor;
import org.edx.mobile.http.interceptor.NewVersionBroadcastInterceptor;
import org.edx.mobile.http.interceptor.NoCacheHeaderStrippingInterceptor;
import org.edx.mobile.http.interceptor.OauthHeaderRequestInterceptor;
import org.edx.mobile.http.interceptor.StaleIfErrorHandlingInterceptor;
import org.edx.mobile.http.interceptor.StaleIfErrorInterceptor;
import org.edx.mobile.http.interceptor.UserAgentInterceptor;
import org.edx.mobile.http.util.Tls12SocketFactory;
import org.edx.mobile.tta.data.model.HtmlResponse;
import org.edx.mobile.util.BrowserUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
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

    private static final int cacheSize = 10 * 1024 * 1024; // 10 MiB

    public static IRemoteDataSource create(Context context, boolean isTargetedForJson) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        List<Interceptor> interceptors = builder.interceptors();

        final File cacheDirectory = new File(context.getFilesDir(), "http-cache");
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        final Cache cache = new Cache(cacheDirectory, cacheSize);
        builder.cache(cache);
        interceptors.add(new StaleIfErrorInterceptor());
        interceptors.add(new StaleIfErrorHandlingInterceptor());
        interceptors.add(new CustomCacheQueryInterceptor(context));
        builder.networkInterceptors().add(new NoCacheHeaderStrippingInterceptor());

        interceptors.add(new JsonMergePatchInterceptor());
        interceptors.add(new UserAgentInterceptor(
                System.getProperty("http.agent") + " " +
                        context.getString(R.string.app_name) + "/" +
                        BuildConfig.APPLICATION_ID + "/" +
                        BuildConfig.VERSION_NAME));

        interceptors.add(new OauthHeaderRequestInterceptor(context));

        interceptors.add(new NewVersionBroadcastInterceptor());
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            interceptors.add(loggingInterceptor);
        }
        builder.authenticator(new OauthRefreshTokenAuthenticator(context));

        OkHttpClient client = Tls12SocketFactory.enableTls12OnPreLollipop(builder).build();

        //BuildConfig.DEBUG
        /*if (true) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        OkHttpClient client = builder.addInterceptor(chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader("x-zhsq-code", "zhsq-u3254p-app")
                .addHeader("x-zhsq-app", "user");

            final String token = BrowserUtil.loginPrefs.getAuthorizationHeader();
            if (token != null) {
                requestBuilder.addHeader("Authorization", token);
            }
                // add your header here

            return chain.proceed(requestBuilder.build());
        })
            .connectTimeout(IRemoteDataSource.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(IRemoteDataSource.READ_TIMEOUT, TimeUnit.SECONDS)
            //.addNetworkInterceptor(new StethoInterceptor())
            .build();*/

        Gson gson = new GsonBuilder().serializeNulls().create();

        Retrofit.Builder retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(IRemoteDataSource.BASE_URL);
            //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        if (isTargetedForJson) {
            retrofit.addConverterFactory(GsonConverterFactory.create(gson));
        } else {
            retrofit.addConverterFactory(HtmlResponse.HtmlResponseConverter.FACTORY);
        }
        return retrofit.build()
            .create(IRemoteDataSource.class);
    }
}
