package org.edx.mobile.test.http;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.edx.mobile.http.GzipRequestInterceptor;
import org.edx.mobile.http.OauthHeaderRequestInterceptor;
import org.edx.mobile.http.OfflineRequestInterceptor;
import org.edx.mobile.test.BaseTestCase;
import org.junit.Ignore;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.GET;
import retrofit.http.Headers;

import static org.assertj.core.api.Assertions.assertThat;
@Ignore
public class RetrofitOkhttpTestCase extends BaseTestCase {

    interface DummyService {
        @Headers("Cache-Control: no-cache")
        @GET("/endpoint1")  Object getRequestNoCache(String nocache);

        @GET("/endpoint1")  Object getRequest(String nocache);
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
        OkHttpClient oauthBasedClient = new OkHttpClient();
        File cacheDirectory = new File(context.getCacheDir(), "http-cache-test");
        if (cacheDirectory.exists()) {
            cacheDirectory.delete();
        }
        cacheDirectory.mkdir();
        final int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(cacheDirectory, cacheSize);
        oauthBasedClient.setCache(cache);
        List<Interceptor> interceptors = oauthBasedClient.interceptors();
        interceptors.add(new GzipRequestInterceptor());
        interceptors.add(new OauthHeaderRequestInterceptor(context));
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        interceptors.add(loggingInterceptor);

        Executor executor = Executors.newCachedThreadPool();
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setExecutors(executor, executor)
            .setClient(new OkClient(oauthBasedClient))
            .setEndpoint(server.getUrl("/").toString())
            .setRequestInterceptor(new OfflineRequestInterceptor(context) {
                public boolean isNetworkConnected() {
                    return isNetworkConnected;
                }
            })
            .build();

        DummyService service = restAdapter.create(DummyService.class);

        //test online
        isNetworkConnected = true;
        service.getRequest("test");

        assertThat(cache.getRequestCount()).isEqualTo(1);
        assertThat(cache.getNetworkCount()).isEqualTo(1);
        assertThat(cache.getHitCount()).isEqualTo(0);

        service.getRequest("test");
        assertThat(cache.getRequestCount()).isEqualTo(2);
        assertThat(cache.getNetworkCount()).isEqualTo(1);
        assertThat(cache.getHitCount()).isEqualTo(1);

        service.getRequestNoCache("test");
        assertThat(cache.getRequestCount()).isEqualTo(3);
        assertThat(cache.getNetworkCount()).isEqualTo(2);
        assertThat(cache.getHitCount()).isEqualTo(2);

        //test offline
        isNetworkConnected = false;
        service.getRequest("test");
        assertThat(cache.getRequestCount()).isEqualTo(4);
        assertThat(cache.getNetworkCount()).isEqualTo(2);
        assertThat(cache.getHitCount()).isEqualTo(2);

        //TODO - what's that?
        service.getRequestNoCache("test");
        assertThat(cache.getRequestCount()).isEqualTo(4);
        assertThat(cache.getNetworkCount()).isEqualTo(2);
        assertThat(cache.getHitCount()).isEqualTo(2);
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
                return new MockResponse().setResponseCode(200);
            }
            return new MockResponse().setResponseCode(404);
        }
    };
}
