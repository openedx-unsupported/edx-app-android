package org.edx.mobile.test.http;

import android.content.Context;

import org.edx.mobile.http.interceptor.GzipRequestInterceptor;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.interceptor.OauthHeaderRequestInterceptor;
import org.edx.mobile.test.BaseTestCase;
import org.junit.Ignore;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;

import static org.assertj.core.api.Java6Assertions.assertThat;
@Ignore
public class RetrofitOkhttpTestCase extends BaseTestCase {

    interface DummyService {
        @Headers("Cache-Control: no-cache")
        @GET("/endpoint1")  Call<Object> getRequestNoCache(String nocache);

        @GET("/endpoint1")  Call<Object> getRequest(String nocache);
    }
    // Use a mock server to serve fixed responses
    protected MockWebServer server;
    protected Context context;

    private boolean isNetworkConnected;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        server = new MockWebServer();
        server.setDispatcher(dispatcher);
        server.start();

        context = RuntimeEnvironment.application;

    }

    public void testCache() throws Exception {
        OkHttpClient.Builder oauthBasedClientBuilder = new OkHttpClient.Builder();
        File cacheDirectory = new File(context.getCacheDir(), "http-cache-test");
        if (cacheDirectory.exists()) {
            cacheDirectory.delete();
        }
        cacheDirectory.mkdir();
        final int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(cacheDirectory, cacheSize);
        oauthBasedClientBuilder.cache(cache);
        List<Interceptor> interceptors = oauthBasedClientBuilder.interceptors();
        interceptors.add(new GzipRequestInterceptor());
        interceptors.add(new OauthHeaderRequestInterceptor(context));
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        interceptors.add(loggingInterceptor);
        OkHttpClient oauthBasedClient = oauthBasedClientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(oauthBasedClient)
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DummyService service = retrofit.create(DummyService.class);

        //test online
        isNetworkConnected = true;
        service.getRequest("test").execute();

        assertThat(cache.requestCount()).isEqualTo(1);
        assertThat(cache.networkCount()).isEqualTo(1);
        assertThat(cache.hitCount()).isEqualTo(0);

        service.getRequest("test").execute();
        assertThat(cache.requestCount()).isEqualTo(2);
        assertThat(cache.networkCount()).isEqualTo(1);
        assertThat(cache.hitCount()).isEqualTo(1);

        service.getRequestNoCache("test").execute();
        assertThat(cache.requestCount()).isEqualTo(3);
        assertThat(cache.networkCount()).isEqualTo(2);
        assertThat(cache.hitCount()).isEqualTo(2);

        //test offline
        isNetworkConnected = false;
        service.getRequest("test").execute();
        assertThat(cache.requestCount()).isEqualTo(4);
        assertThat(cache.networkCount()).isEqualTo(2);
        assertThat(cache.hitCount()).isEqualTo(2);

        //TODO - what's that?
        service.getRequestNoCache("test").execute();
        assertThat(cache.requestCount()).isEqualTo(4);
        assertThat(cache.networkCount()).isEqualTo(2);
        assertThat(cache.hitCount()).isEqualTo(2);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        server.shutdown();
    }

    final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            if (request.getPath().equals("/endpoint1")){
                return new MockResponse().setResponseCode(HttpStatus.OK);
            }
            return new MockResponse().setResponseCode(HttpStatus.NOT_FOUND);
        }
    };
}
