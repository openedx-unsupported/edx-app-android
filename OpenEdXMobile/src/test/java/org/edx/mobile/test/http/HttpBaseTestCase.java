package org.edx.mobile.test.http;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.inject.Injector;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.course.CourseService;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.interceptor.JsonMergePatchInterceptor;
import org.edx.mobile.http.interceptor.OnlyIfCachedStrippingInterceptor;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.test.util.MockDataUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.robolectric.android.util.concurrent.RoboExecutorService;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertNotNull;

/**
 * use MockWebService for Api test
 */
@Ignore
public class HttpBaseTestCase extends BaseTestCase {
    private static final int DELAY_MS = 2000; // Network calls will take 2 seconds.
    private static final int VARIANCE_PCT = 40; // Network delay varies by ±40%.
    private static final int ERROR_PCT = 3; // 3% of network calls will fail.
    private static final int ERROR_DELAY_FACTOR = 3; // Network errors will be scaled by this value.
    private static final Random random = new Random(); // Random instance for determining delays
    private static final String API_HOST_URL = "API_HOST_URL"; // Config key for API host url
    // Use a mock server to serve fixed responses
    protected MockWebServer server;
    // Per-test configuration for whether the mock web server should create artificial delays
    // before sending the response.
    protected boolean useArtificialDelay = false;
    protected OkHttpClient okHttpClient;
    protected LoginAPI loginAPI;
    protected LoginService loginService;
    protected CourseAPI courseAPI;
    protected CourseService courseService;

    /**
     * Returns the base url used by the mock server
     */
    private String getBaseMockUrl() {
        return "http://" + server.getHostName() + ":" + server.getPort();
    }

    @Override
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.setDispatcher(new MockResponseDispatcher());
        server.start();

        okHttpClient = new OkHttpClient.Builder()
                .dispatcher(new Dispatcher(new RoboExecutorService()))
                .addInterceptor(new JsonMergePatchInterceptor())
                .addInterceptor(new OnlyIfCachedStrippingInterceptor())
                .build();

        super.setUp();
    }

    @Override
    protected JsonObject generateConfigProperties() throws IOException {
        // Add the mock host url in the test config properties
        JsonObject properties = super.generateConfigProperties();
        properties.addProperty(API_HOST_URL, getBaseMockUrl());
        return properties;
    }

    @Override
    public void addBindings() {
        super.addBindings();
        module.addBinding(OkHttpClient.class, okHttpClient);
        module.addBinding(OkHttpClientProvider.class, new OkHttpClientProvider() {
            @Override
            public OkHttpClient getWithOfflineCache() {
                return okHttpClient;
            }

            @Override
            public OkHttpClient getNonOAuthBased() {
                return okHttpClient;
            }

            @Override
            public OkHttpClient get() {
                return okHttpClient;
            }
        });
    }

    @Override
    protected void inject(Injector injector) throws Exception {
        super.inject(injector);
        loginAPI = injector.getInstance(LoginAPI.class);
        loginService = injector.getInstance(LoginService.class);
        courseAPI = injector.getInstance(CourseAPI.class);
        courseService = injector.getInstance(CourseService.class);
    }

    /**
     * Utility method to be used as a prerequisite for testing most API
     *
     * @throws Exception If an exception was encountered during login or
     *                   verification
     */
    protected void login() throws Exception {
        // The credentials given here don't matter, we will always get the same mock response
        AuthResponse res = loginAPI.logInUsingEmail("example@example.com", "password");
        assertNotNull(res);
        assertNotNull(res.access_token);
        assertNotNull(res.token_type);
        assertNotNull(res.refresh_token);
        print(res.toString());
        assertNotNull(res.profile);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        server.shutdown();
    }

    /**
     * Randomly determine whether this call should result in a network failure.
     */
    private static boolean calculateIsFailure() {
        int randomValue = random.nextInt(100) + 1;
        return randomValue <= ERROR_PCT;
    }

    // The delay randomizing methods below are copied from the Retrofit
    // MockRestAdapter implementation which is distributed under the Apache 2.0
    // License

    /**
     * Get the delay (in milliseconds) that should be used for triggering a
     * network error.
     * <p/>
     * Because we are triggering an error, use a random delay between 0 and
     * three times the normal network delay to simulate a flaky connection
     * failing anywhere from quickly to slowly.
     */
    private static int calculateDelayForError() {
        return random.nextInt(DELAY_MS * ERROR_DELAY_FACTOR);
    }

    /**
     * Get the delay (in milliseconds) that should be used for delaying
     * a successful network call response.
     */
    private static int calculateDelayForSuccess() {
        float errorPercent = VARIANCE_PCT / 100f; // e.g., 20 / 100f == 0.2f
        float lowerBound = 1f - errorPercent; // 0.2f --> 0.8f
        float upperBound = 1f + errorPercent; // 0.2f --> 1.2f
        float bound = upperBound - lowerBound; // 1.2f - 0.8f == 0.4f
        float delayPercent = (random.nextFloat() * bound) +
                lowerBound; // 0.8 + (rnd * 0.4)
        return (int) (DELAY_MS * delayPercent);
    }

    /**
     * Get the delay (in milliseconds) that should be used for delaying
     * a network call response.
     */
    private static int calculateDelayForCall() {
        // Commenting out the random failure mode delay since we want our
        // tests to be reproducible
        return //calculateIsFailure() ? calculateDelayForError() :
                calculateDelayForSuccess();
    }

    /**
     * Match url to a regex template while allowing extra slash and query
     * strings at the end
     */
    private static boolean urlMatches(String url, String template) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(template)) {
            return false;
        }
        String pattern = '^' + template;
        if (template.charAt(template.length() - 1) != '/') {
            pattern += "/?";
        }
        pattern += "(\\?.*)?$";
        return url.matches(pattern);
    }

    private MockResponse generateMockResponse(RecordedRequest request) {
        final String method = request.getMethod();
        final String path = request.getPath();
        final String body = request.getBody().readUtf8();
        MockResponse response = new MockResponse();
        response.addHeader("Set-Cookie", "csrftoken=dummy; Max-Age=31449600; Path=/");
        response.setResponseCode(HttpStatus.NOT_FOUND);
        try {
            if ("POST".equals(method)) {
                if (urlMatches(path, "/oauth2/access_token")) {
                    response.setBody(MockDataUtil.getMockResponse("post_oauth2_access_token"));
                    response.setResponseCode(HttpStatus.OK);
                } else if (urlMatches(path, "/api/mobile/v0.5/users/staff/course_status_info/[^/]+/[^/]+/[^/]+")) {
                    try {
                        JSONObject jsonObject = new JSONObject(request.getBody().readUtf8());
                        String moduleId = jsonObject.getString("last_visited_module_id");
                        response.setBody(String.format(Locale.US, MockDataUtil.getMockResponse("post_course_status_info"), moduleId));
                        response.setResponseCode(HttpStatus.OK);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (urlMatches(path, "/api/enrollment/v1/enrollment")) {
                    try {
                        JSONObject jsonObject = new JSONObject(request.getBody().readUtf8());
                        response.setBody(String.format(Locale.US, MockDataUtil.getMockResponse("post_enrollment"),
                                jsonObject.getJSONObject("course_details").getString("course_id")));
                        response.setResponseCode(HttpStatus.OK);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (urlMatches(path, "/password_reset")) {
                    response.setBody(MockDataUtil.getMockResponse("post_password_reset"));
                    response.setResponseCode(HttpStatus.OK);
                }
            } else if ("GET".equals(method)) {
                if (urlMatches(path, "/api/mobile/v0.5/my_user_info")) {
                    String baseMockUrl = getBaseMockUrl();
                    response.setBody(String.format(Locale.US, MockDataUtil.getMockResponse("get_my_user_info"), baseMockUrl));
                    response.setResponseCode(HttpStatus.OK);
                } else if (urlMatches(path, "/api/mobile/v0.5/users/[^/]+/course_enrollments")) {
                    String baseMockUrl = getBaseMockUrl();
                    response.setBody(String.format(Locale.US, MockDataUtil.getMockResponse("get_course_enrollments"), baseMockUrl));
                    response.setResponseCode(HttpStatus.OK);
                } else if (urlMatches(path, "/api/mobile/v0.5/video_outlines/courses/[^/]+/[^/]+/[^/]+")) {
                    response.setBody(MockDataUtil.getMockResponse("get_video_outlines_courses"));
                    response.setResponseCode(HttpStatus.OK);
                } else if (urlMatches(path, "/api/mobile/v0.5/course_info/[^/]+/[^/]+/[^/]+/updates")) {
                    response.setBody(MockDataUtil.getMockResponse("get_course_info_updates"));
                    response.setResponseCode(HttpStatus.OK);
                } else if (urlMatches(path, "/api/mobile/v0.5/users/staff/course_status_info/[^/]+/[^/]+/[^/]+")) {
                    Matcher matcher = Pattern.compile(
                            "/api/mobile/v0.5/users/staff/course_status_info/([^/]+)/([^/]+)/([^/]+)", 0).matcher(path);
                    matcher.matches();
                    String moduleId = "i4x://" + matcher.group(1) + '/' + matcher.group(2) + "/course/" + matcher.group(3);
                    response.setBody(String.format(Locale.US, MockDataUtil.getMockResponse("get_course_status_info"), moduleId));
                    response.setResponseCode(HttpStatus.OK);
                } else if (urlMatches(path, "/api/mobile/v0.5/course_info/[^/]+/[^/]+/[^/]+/handouts")) {
                    // TODO: Find out if this is a wrong API call or server issue
                    response.setResponseCode(HttpStatus.NOT_FOUND);
                    response.setBody("{\"detail\": \"Not found\"}");
                } else if (urlMatches(path, "/api/courses/v1/blocks/")) {
                    // TODO: Return different responses based on the parameters?
                    response.setBody(MockDataUtil.getMockResponse("get_course_structure"));
                    response.setResponseCode(HttpStatus.OK);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Handler for requests on the mock server that will send mock responses
     * according to the request urls
     */
    private class MockResponseDispatcher extends okhttp3.mockwebserver.Dispatcher {
        @Override
        public MockResponse dispatch(RecordedRequest recordedRequest)
                throws InterruptedException {
            if (useArtificialDelay) {
                Thread.sleep(calculateDelayForCall());
            }
            return generateMockResponse(recordedRequest);
        }
    }

}
