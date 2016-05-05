package org.edx.mobile.test;

import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.edx.mobile.http.OauthRefreshTokenAuthenticator;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.test.util.MockDataUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.edx.mobile.test.util.OkHttpTestUtil.defaultClient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Config(sdk = 18)
public final class AuthenticationTests extends BaseTestCase {

    private static final String API_HOST_URL = "API_HOST_URL"; // Config key for API host url

    @Rule
    public final MockWebServer mockServer = new MockWebServer();

    private OkHttpClient client = defaultClient();

    @Before
    public void setUp() throws Exception {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        pref.put(PrefManager.Key.AUTH_JSON, MockDataUtil.getMockResponse("post_oauth2_access_token"));
        mockServer.setDispatcher(dispatcher);
        super.setUp();
    }

    @Override
    protected JsonObject generateConfigProperties() throws IOException {
        // Add the mock host url in the test config properties
        JsonObject properties = super.generateConfigProperties();
        properties.addProperty(API_HOST_URL, mockServer.url("/").toString());
        return properties;
    }

    @Test
    public void testAuthenticate_forExpiredAccessToken() throws Exception {
        // create a client with the authenticator
        client = client.newBuilder()
                .authenticator(new OauthRefreshTokenAuthenticator(context))
                .build();

        // Build a new dummy request to trigger authenticator
        Request request = new Request.Builder()
                .url(mockServer.url("/dummy/endpoint/"))
                .header("Authorization", "expired_token")
                .build();

        // Make request
        Response response = client.newCall(request).execute();
        assertEquals(200, response.code());
        assertEquals("Bearer dummy", response.request().header("Authorization"));

        // Assert the expired token request was sent
        RecordedRequest expiredRequest = mockServer.takeRequest();
        assertEquals("/dummy/endpoint/", expiredRequest.getPath());
        assertEquals("expired_token", expiredRequest.getHeader("Authorization"));

        // Assert the authenticator requests for a new access token using the refresh token
        RecordedRequest refreshTokenRequest = mockServer.takeRequest();
        assertEquals("/oauth2/access_token/", refreshTokenRequest.getPath());
        String actual_body = refreshTokenRequest.getBody().readUtf8();
        assertTrue(actual_body.contains("grant_type=refresh_token"));
        assertTrue(actual_body.contains("refresh_token=dummy_refresh_token"));

        // Assert that the original request was made again with the new token.
        RecordedRequest refreshedRequest = mockServer.takeRequest();
        assertEquals("/dummy/endpoint/", refreshedRequest.getPath());
        assertEquals("Bearer dummy", refreshedRequest.getHeader("Authorization"));
    }

    @Test
    public void testAuthenticate_notForExpiredAccessToken() throws Exception {
        client = client.newBuilder()
                .authenticator(new OauthRefreshTokenAuthenticator(context))
                .build();

        Request request = new Request.Builder()
                .url(mockServer.url("/dummy/endpoint/"))
                .header("Authorization", "401_not_caused_by_expired_token")
                .build();

        Response response = client.newCall(request).execute();
        assertEquals(401, response.code());
        assertEquals("401_not_caused_by_expired_token", response.request().header("Authorization"));
    }

    final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            MockResponse response = new MockResponse();

            String path = request.getPath();
            String header = request.getHeader("Authorization");

            response.setResponseCode(404);
            try {
                if (path.equals("/oauth2/access_token/")) {
                    response.setResponseCode(200).setBody(MockDataUtil.getMockResponse("post_oauth2_access_token"));
                } else if (path.equals("/dummy/endpoint/")) {
                    if (header.equals("expired_token")) {
                        response.setResponseCode(401)
                                .addHeader("Authorization", "old_access_token")
                                .setBody(MockDataUtil.getMockResponse("401_expired_token_body"));
                    } else if (header.equals("Bearer dummy")) {
                        response.setResponseCode(200);
                    } else if (header.equals("401_not_caused_by_expired_token")) {
                        response.setResponseCode(401);
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return response;
        }
    };
}
