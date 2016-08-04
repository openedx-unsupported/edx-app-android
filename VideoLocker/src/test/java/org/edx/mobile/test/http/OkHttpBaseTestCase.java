package org.edx.mobile.test.http;

import android.text.TextUtils;

import com.google.gson.JsonObject;

import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.test.BaseTestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 *  use MockWebService for Api test
 *  TODO - code refactoring need for OkHttpBaseTestCase and HttpBaseTestCase
 *  we need to depreciate OkHttpBaseTestCase soon.
 */
@Ignore
public class OkHttpBaseTestCase extends BaseTestCase {
    private static final int DELAY_MS = 2000; // Network calls will take 2 seconds.
    private static final int VARIANCE_PCT = 40; // Network delay varies by ±40%.
    private static final int ERROR_PCT = 3; // 3% of network calls will fail.
    private static final int ERROR_DELAY_FACTOR = 3; // Network errors will be scaled by this value.
    private static final Random random = new Random(); // Random instance for determining delays
    private static final String API_HOST_URL = "API_HOST_URL"; // Config key for API host url

    // Use a mock server to serve fixed responses
    protected MockWebServer server;


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
    public void tearDown() throws Exception {
        super.tearDown();
        server.shutdown();
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
        MockResponse response = new MockResponse();
        response.addHeader("Set-Cookie", "csrftoken=dummy; Max-Age=31449600; Path=/");
        response.setResponseCode(HttpStatus.NOT_FOUND);
        if ("POST".equals(method)) {
            if (urlMatches(path, "/oauth2/access_token")) {
                response.setResponseCode(HttpStatus.OK);
                response.setBody("{\"access_token\": \"dummy\", \"token_type\": \"Bearer\", \"expires_in\": 2591999, \"scope\": \"\"}");
            } else if (urlMatches(path, "/api/mobile/v0.5/users/staff/course_status_info/[^/]+/[^/]+/[^/]+")) {
                try {
                    JSONObject body = new JSONObject(request.getBody().readUtf8());
                    String moduleId = body.getString("last_visited_module_id");
                    response.setResponseCode(HttpStatus.OK);
                    response.setBody("{\"last_visited_module_id\": \"" + moduleId + "\", \"last_visited_module_path\": [\"" + moduleId + "\"]}");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (urlMatches(path, "/api/enrollment/v1/enrollment")) {
                try {
                    JSONObject body = new JSONObject(request.getBody().readUtf8());
                    response.setResponseCode(HttpStatus.OK);
                    response.setBody("{\"created\": \"2015-04-21T18:25:55Z\", \"mode\": \"honor\", \"is_active\": true, \"course_details\": {\"course_end\": null, \"course_start\": \"2030-01-01T00:00:00Z\", \"course_modes\": [{\"slug\": \"honor\", \"name\": \"Honor Code Certificate\", \"min_price\": 0, \"suggested_prices\": [], \"currency\": \"usd\", \"expiration_datetime\": null, \"description\": null, \"sku\": null}], \"enrollment_start\": null, \"enrollment_end\": null, \"invite_only\": false, \"course_id\": \""
                            + body.getJSONObject("course_details").getString("course_id") + "\"}, \"user\": \"staff\"}");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (urlMatches(path, "/password_reset")) {
                response.setResponseCode(HttpStatus.OK);
                response.setBody("{\"value\": \"\\n<header>\\n  <h2>Password reset successful</h2>\\n  <hr>\\n</header>\\n\\n<div class=\\\"message\\\">\\n  <p>We've e-mailed you instructions for setting your password to the e-mail address you submitted. You should be receiving it shortly.</p>\\n</div>\\n\", \"success\": true}");
            }
        } else if ("GET".equals(method)) {
            if (urlMatches(path, "/api/mobile/v0.5/my_user_info")) {
                String baseMockUrl = getBaseMockUrl();
                response.setResponseCode(HttpStatus.OK);
                response.setBody("{\"id\": 4, \"username\": \"staff\", \"email\": \"staff@example.com\", \"name\": \"staff\", \"course_enrollments\": \"" + baseMockUrl + "/api/mobile/v0.5/users/staff/course_enrollments/\"}");
            } else if (urlMatches(path, "/api/mobile/v0.5/users/[^/]+/course_enrollments")) {
                String baseMockUrl = getBaseMockUrl();
                response.setResponseCode(HttpStatus.OK);
                response.setBody("[{\"created\": \"2015-04-21T18:25:55Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/knockknock.org/kk001/2015_T1/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/knockknock.org/kk001/2015_T1/updates\", \"number\": \"kk001\", \"org\": \"knockknock.org\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/knockknock.org/kk001/2015_T1\", \"id\": \"knockknock.org/kk001/2015_T1\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"knockknockwhoisthere\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/knockknock.org/kk001/2015_T1/handouts\", \"start\": \"2030-01-01T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_NNXG6Y3LNNXG6Y3LFZXXEZZPNNVTAMBRF4ZDAMJVL5KDC___\", \"course_image\": \"/c4x/knockknock.org/kk001/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2015-03-06T20:21:37Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/McGillX/Body101x/1T2015/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/McGillX/Body101x/1T2015/updates\", \"number\": \"Body101x\", \"org\": \"McGillX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/McGillX/Body101x/1T2015\", \"id\": \"McGillX/Body101x/1T2015\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"The Body Matters\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/McGillX/Body101x/1T2015/handouts\", \"start\": \"2030-01-01T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JVRUO2LMNRMC6QTPMR4TCMBRPAXTCVBSGAYTK___\", \"course_image\": \"/c4x/McGillX/Body101x/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2015-03-06T20:19:18Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/SMES/PSYCH101x/1T2015/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/SMES/PSYCH101x/1T2015/updates\", \"number\": \"PSYCH101x\", \"org\": \"SMES\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/SMES/PSYCH101x/1T2015\", \"id\": \"SMES/PSYCH101x/1T2015\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"Introductory Psychology\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/SMES/PSYCH101x/1T2015/handouts\", \"start\": \"2030-01-01T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_KNGUKUZPKBJVSQ2IGEYDC6BPGFKDEMBRGU______\", \"course_image\": \"/c4x/SMES/PSYCH101x/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2015-03-06T20:15:39Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/UC3Mx/IT.1.1x/1T2015/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/UC3Mx/IT.1.1x/1T2015/updates\", \"number\": \"IT.1.1x\", \"org\": \"UC3Mx\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/UC3Mx/IT.1.1x/1T2015\", \"id\": \"UC3Mx/IT.1.1x/1T2015\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"Introduction to Programming with Java - Part 1\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/UC3Mx/IT.1.1x/1T2015/handouts\", \"start\": \"2030-01-01T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_KVBTGTLYF5EVILRRFYYXQLZRKQZDAMJV\", \"course_image\": \"/c4x/UC3Mx/IT.1.1x/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-12-30T17:31:59Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/apple/banana/carrot/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/apple/banana/carrot/updates\", \"number\": \"banana\", \"org\": \"apple\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/apple/banana/carrot\", \"id\": \"apple/banana/carrot\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"splittest\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/apple/banana/carrot/handouts\", \"start\": \"2030-01-01T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_MFYHA3DFF5RGC3TBNZQS6Y3BOJZG65A_\", \"course_image\": \"/c4x/apple/banana/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-12-30T17:17:55Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/split/split/split/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/split/split/split/updates\", \"number\": \"split\", \"org\": \"split\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/split/split/split\", \"id\": \"split/split/split\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"Split Test Module\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/split/split/split/handouts\", \"start\": \"2030-01-01T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_ONYGY2LUF5ZXA3DJOQXXG4DMNF2A____\", \"course_image\": \"/c4x/split/split/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-11-25T15:48:41Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/15.390.1x/3T2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/15.390.1x/3T2014/updates\", \"number\": \"15.390.1x\", \"org\": \"MITx\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/MITx/15.390.1x/3T2014\", \"id\": \"MITx/15.390.1x/3T2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2015-03-31T00:00:00Z\", \"name\": \"Entrepreneurship 101: Who is your customer?\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/15.390.1x/3T2014/handouts\", \"start\": \"2015-01-09T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JVEVI6BPGE2S4MZZGAXDC6BPGNKDEMBRGQ______\", \"course_image\": \"/c4x/MITx/15.390.1x/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-11-24T19:51:07Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edx/1/2/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edx/1/2/updates\", \"number\": \"1\", \"org\": \"edx\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/edx/1/2\", \"id\": \"edx/1/2\", \"latest_updates\": {\"video\": null}, \"end\": \"2015-01-12T23:00:00Z\", \"name\": \"Paradigms of Computer Programming - Abstraction and Concurrency\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edx/1/2/handouts\", \"start\": \"2014-11-16T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_MVSHQLZRF4ZA____\", \"course_image\": \"/c4x/edx/1/asset/Illu_LouvainX1_262x136.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-11-21T19:13:15Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/Lou/LVM101/2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/Lou/LVM101/2014/updates\", \"number\": \"LVM101\", \"org\": \"Lou\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/Lou/LVM101/2014\", \"id\": \"Lou/LVM101/2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2015-01-12T23:00:00Z\", \"name\": \"Paradigms of Computer Programming - Abstraction and Concurrency\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/Lou/LVM101/2014/handouts\", \"start\": \"2014-11-16T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JRXXKL2MKZGTCMBRF4ZDAMJU\", \"course_image\": \"/c4x/Lou/LVM101/asset/Illu_LouvainX1_262x136.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:37:33Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/UQx/World101x/3T2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/UQx/World101x/3T2014/updates\", \"number\": \"World101x\", \"org\": \"UQx\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/UQx/World101x/3T2014\", \"id\": \"UQx/World101x/3T2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-10-26T23:59:00Z\", \"name\": \"Anthropology of Current World Issues\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/UQx/World101x/3T2014/handouts\", \"start\": \"2014-08-25T09:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_KVIXQL2XN5ZGYZBRGAYXQLZTKQZDAMJU\", \"course_image\": \"/c4x/UQx/World101x/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:36:17Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/LouvainX/Louv3.02x/3T2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/LouvainX/Louv3.02x/3T2014/updates\", \"number\": \"Louv3x\", \"org\": \"LouvainX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/LouvainX/Louv3.02x/3T2014\", \"id\": \"LouvainX/Louv3.02x/3T2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-11-15T00:00:00Z\", \"name\": \"D\\u00e9couvrir la science politique\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/LouvainX/Louv3.02x/3T2014/handouts\", \"start\": \"2014-09-25T00:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JRXXK5TBNFXFQL2MN52XMMZOGAZHQLZTKQZDAMJU\", \"course_image\": \"/c4x/LouvainX/Louv3.02x/asset/Illu_LouvainX3-136x262.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:34:36Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/LouvainX/Louv1.1x/3T2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/LouvainX/Louv1.1x/3T2014/updates\", \"number\": \"Louv1.1x\", \"org\": \"LouvainX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/LouvainX/Louv1.1x/3T2014\", \"id\": \"LouvainX/Louv1.1x/3T2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-11-30T22:59:00Z\", \"name\": \"Paradigms of Computer Programming - Fundamentals\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/LouvainX/Louv1.1x/3T2014/handouts\", \"start\": \"2014-09-22T10:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JRXXK5TBNFXFQL2MN52XMMJOGF4C6M2UGIYDCNA_\", \"course_image\": \"/c4x/LouvainX/Louv1.1x/asset/Illu_LouvainX1_262x136.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:32:34Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/6.002x_4x/3T2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/6.002x_4x/3T2014/updates\", \"number\": \"6.002x\", \"org\": \"MITx\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/MITx/6.002x_4x/3T2014\", \"id\": \"MITx/6.002x_4x/3T2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-12-15T05:00:00Z\", \"name\": \"Circuits and Electronics\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/6.002x_4x/3T2014/handouts\", \"start\": \"2014-08-25T15:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JVEVI6BPGYXDAMBSPBPTI6BPGNKDEMBRGQ______\", \"course_image\": \"/c4x/MITx/6.002x_4x/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:30:50Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/4.605x_2/3T2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/4.605x_2/3T2014/updates\", \"number\": \"4.605x\", \"org\": \"MITx\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/MITx/4.605x_2/3T2014\", \"id\": \"MITx/4.605x_2/3T2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-12-11T05:00:00Z\", \"name\": \"A Global History of Architecture: Part 1\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/MITx/4.605x_2/3T2014/handouts\", \"start\": \"2014-09-23T14:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JVEVI6BPGQXDMMBVPBPTELZTKQZDAMJU\", \"course_image\": \"/c4x/MITx/4.605x_2/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:29:39Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/HarvardX/AmPoX.1/2014_T3/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/HarvardX/AmPoX.1/2014_T3/updates\", \"number\": \"AmPoX.1\", \"org\": \"HarvardX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/HarvardX/AmPoX.1/2014_T3\", \"id\": \"HarvardX/AmPoX.1/2014_T3\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-10-17T21:00:00Z\", \"name\": \"Poetry in America: The Poetry of Early New England\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/HarvardX/AmPoX.1/2014_T3/handouts\", \"start\": \"2014-09-10T16:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_JBQXE5TBOJSFQL2BNVIG6WBOGEXTEMBRGRPVIMY_\", \"course_image\": \"/c4x/HarvardX/AmPoX.1/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:28:30Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/DelftX/ET.3034TU/3T2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/DelftX/ET.3034TU/3T2014/updates\", \"number\": \"ET.3034TU\", \"org\": \"DelftX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/DelftX/ET.3034TU/3T2014\", \"id\": \"DelftX/ET.3034TU/3T2014\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-12-06T10:00:00Z\", \"name\": \"Solar Energy\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/DelftX/ET.3034TU/3T2014/handouts\", \"start\": \"2014-09-01T09:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_IRSWYZTULAXUKVBOGMYDGNCUKUXTGVBSGAYTI___\", \"course_image\": \"/c4x/DelftX/ET.3034TU/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:18:30Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edX/DemoX.1/2014/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edX/DemoX.1/2014/updates\", \"number\": \"DemoX.1\", \"org\": \"edX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/edX/DemoX.1/2014\", \"id\": \"edX/DemoX.1/2014\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"Demo Course\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edX/DemoX.1/2014/handouts\", \"start\": \"2014-08-01T04:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_MVSFQL2EMVWW6WBOGEXTEMBRGQ______\", \"course_image\": \"/c4x/edX/DemoX.1/asset/CourseListingImage.png\"}, \"certificate\": {}}, {\"created\": \"2014-09-19T19:16:13Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/WellesleyX/SOC108x/2014_SOND/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/WellesleyX/SOC108x/2014_SOND/updates\", \"number\": \"SOC108x\", \"org\": \"WellesleyX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/WellesleyX/SOC108x/2014_SOND\", \"id\": \"WellesleyX/SOC108x/2014_SOND\", \"latest_updates\": {\"video\": null}, \"end\": \"2014-11-10T23:30:00Z\", \"name\": \"Introduction to Global Sociology\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/WellesleyX/SOC108x/2014_SOND/handouts\", \"start\": \"2014-09-02T19:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_K5SWY3DFONWGK6KYF5JU6QZRGA4HQLZSGAYTIX2TJ5HEI___\", \"course_image\": \"/c4x/WellesleyX/SOC108x/asset/images_course_image.jpg\"}, \"certificate\": {}}, {\"created\": \"2014-08-25T16:38:08Z\", \"mode\": \"honor\", \"is_active\": true, \"course\": {\"course_about\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edX/DemoX/Demo_Course/about\", \"course_updates\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edX/DemoX/Demo_Course/updates\", \"number\": \"DemoX\", \"org\": \"edX\", \"video_outline\": \"" + baseMockUrl + "/api/mobile/v0.5/video_outlines/courses/edX/DemoX/Demo_Course\", \"id\": \"edX/DemoX/Demo_Course\", \"latest_updates\": {\"video\": null}, \"end\": null, \"name\": \"edX Demonstration Course\", \"course_handouts\": \"" + baseMockUrl + "/api/mobile/v0.5/course_info/edX/DemoX/Demo_Course/handouts\", \"start\": \"2013-02-05T05:00:00Z\", \"social_urls\": {\"facebook\": null}, \"subscription_id\": \"course_MVSFQL2EMVWW6WBPIRSW2327INXXK4TTMU______\", \"course_image\": \"/c4x/edX/DemoX/asset/images_course_image.jpg\"}, \"certificate\": {}}]");
            } else if (urlMatches(path, "/api/mobile/v0.5/video_outlines/courses/[^/]+/[^/]+/[^/]+")) {
                response.setResponseCode(HttpStatus.OK);
                response.setBody("[]");
            } else if (urlMatches(path, "/api/mobile/v0.5/course_info/[^/]+/[^/]+/[^/]+/updates")) {
                response.setResponseCode(HttpStatus.OK);
                response.setBody("[]");
            } else if (urlMatches(path, "/api/mobile/v0.5/users/staff/course_status_info/[^/]+/[^/]+/[^/]+")) {
                Matcher matcher = Pattern.compile(
                        "/api/mobile/v0.5/users/staff/course_status_info/([^/]+)/([^/]+)/([^/]+)", 0).matcher(path);
                matcher.matches();
                String moduleId = "i4x://" + matcher.group(1) + '/' + matcher.group(2) + "/course/" + matcher.group(3);
                response.setResponseCode(HttpStatus.OK);
                response.setBody("{\"last_visited_module_id\": \"" + moduleId + "\", \"last_visited_module_path\": [\"" + moduleId + "\"]}");
            } else if (urlMatches(path, "/api/mobile/v0.5/course_info/[^/]+/[^/]+/[^/]+/handouts")) {
                // TODO: Find out if this is a wrong API call or server issue
                response.setResponseCode(HttpStatus.NOT_FOUND);
                response.setBody("{\"detail\": \"Not found\"}");
            }
        }
        return response;
    }

    /**
     * Handler for requests on the mock server that will send mock responses
     * according to the request urls
     */
    private class MockResponseDispatcher extends Dispatcher {
        @Override
        public MockResponse dispatch(RecordedRequest recordedRequest)
                throws InterruptedException {
            Thread.sleep(calculateDelayForCall());
            return generateMockResponse(recordedRequest);
        }
    }

}
