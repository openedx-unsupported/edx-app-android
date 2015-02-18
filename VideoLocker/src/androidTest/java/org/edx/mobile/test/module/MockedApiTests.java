package org.edx.mobile.test.module;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.module.serverapi.ApiFactory;
import org.edx.mobile.module.serverapi.IApi;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.Environment;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by rohan on 2/17/15.
 *
 * For each test case, follow these 3 steps,
 * 1) Mock the response
 * 2) Run the code
 * 3) Verify the requests
 */
public class MockedApiTests extends BaseTestCase {

    private static final String EMAIL       = "user@example.com";
    private static final String PASSWORD    = "password";
    private static final int PORT           = 8000;

    private IApi api;
    private MockWebServer server;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        api = ApiFactory.getInstance(getInstrumentation().getTargetContext());
        server = new MockWebServer();

        Environment env = new Environment();
        env.setupEnvironment(getInstrumentation().getTargetContext());

        // set test configuration
        JsonObject json = new JsonObject();
        json.add("API_HOST_URL", new JsonPrimitive(String.format("http://localhost:%d", PORT)));
        Config config = new Config(json);
        Config.setInstance(config);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.shutdown();
    }

    private void startServer() throws Exception {
        server.play(PORT);
        URL baseUrl = server.getUrl("/mocked");
        print("mocked URL: " + baseUrl.toString());
    }

    public void testLogin() throws Exception {
        // TODO: mock the response
        JSONObject json = new JSONObject();
        json.put("access_token", "fake-token");

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .setBody(json.toString());

        server.enqueue(mockResponse);

        startServer();

        // run the code
        AuthResponse resp = api.doLogin(EMAIL, PASSWORD);
        assertTrue(resp.isSuccess());

        // TODO: verify the request
    }
}
