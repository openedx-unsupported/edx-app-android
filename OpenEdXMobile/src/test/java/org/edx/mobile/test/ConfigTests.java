package org.edx.mobile.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.util.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by aleffert on 2/6/15.
 */
public class ConfigTests extends BaseTestCase {
    //TODO - should we place constant at a central place?
    /* Config keys */
    private static final String DISCOVERY = "DISCOVERY";
    private static final String COURSE_DISCOVERY = "COURSE";
    private static final String SOCIAL_SHARING = "SOCIAL_SHARING";
    private static final String ZERO_RATING = "ZERO_RATING";
    private static final String FACEBOOK = "FACEBOOK";
    private static final String GOOGLE = "GOOGLE";
    private static final String NEW_RELIC = "NEW_RELIC";
    private static final String SEGMENT_IO = "SEGMENT_IO";
    private static final String WHITE_LIST_OF_DOMAINS = "WHITE_LIST_OF_DOMAINS";
    private static final String BRANCH = "BRANCH";

    private static final String ENABLED = "ENABLED";
    private static final String DISABLED_CARRIERS = "DISABLED_CARRIERS";
    private static final String CARRIERS = "CARRIERS";
    private static final String BASE_URL = "BASE_URL";
    private static final String EXPLORE_SUBJECTS_URL = "EXPLORE_SUBJECTS_URL";
    private static final String TYPE = "TYPE";
    private static final String DETAIL_TEMPLATE = "DETAIL_TEMPLATE";
    private static final String FACEBOOK_APP_ID = "FACEBOOK_APP_ID";
    private static final String KEY = "KEY";
    private static final String SECRET = "SECRET";
    private static final String NEW_RELIC_KEY = "NEW_RELIC_KEY";
    private static final String SEGMENT_IO_WRITE_KEY = "SEGMENT_IO_WRITE_KEY";
    private static final String DOMAINS = "DOMAINS";

    @Test
    public void testZeroRatingNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getZeroRatingConfig().isEnabled());
        assertEquals(config.getZeroRatingConfig().getCarriers().size(), 0);
    }

    @Test
    public void testZeroRatingEmptyConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject socialConfig = new JsonObject();
        configBase.add(ZERO_RATING, socialConfig);

        Config config = new Config(configBase);
        assertFalse(config.getZeroRatingConfig().isEnabled());
        assertEquals(config.getZeroRatingConfig().getCarriers().size(), 0);
    }

    @Test
    public void testZeroRatingConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject zeroRatingConfig = new JsonObject();
        zeroRatingConfig.add(ENABLED, new JsonPrimitive(true));
        configBase.add(ZERO_RATING, zeroRatingConfig);

        ArrayList<String> carrierList = new ArrayList<String>();
        carrierList.add("12345");
        carrierList.add("foo");
        JsonArray carriers = new JsonArray();
        for (String carrier : carrierList) {
            carriers.add(new JsonPrimitive(carrier));
        }
        zeroRatingConfig.add(CARRIERS, carriers);

        ArrayList<String> domainList = new ArrayList<>();
        domainList.add("domain1");
        domainList.add("domain2");
        JsonArray domains = new JsonArray();
        for (String domain : domainList) {
            domains.add(new JsonPrimitive(domain));
        }
        zeroRatingConfig.add(WHITE_LIST_OF_DOMAINS, domains);

        Config config = new Config(configBase);
        assertTrue(config.getZeroRatingConfig().isEnabled());
        assertEquals(carrierList, config.getZeroRatingConfig().getCarriers());
        assertEquals(domainList, config.getZeroRatingConfig().getWhiteListedDomains());
    }

    @Test
    public void testEnrollmentNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);

        assertNotNull(config.getDiscoveryConfig());
        assertNull(config.getDiscoveryConfig().getCourseDiscoveryConfig());
        assertNull(config.getDiscoveryConfig().getProgramDiscoveryConfig());
    }

    @Test
    public void testEnrollmentEmptyConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject discoveryConfig = new JsonObject();
        configBase.add(DISCOVERY, discoveryConfig);
        JsonObject courseDiscoveryConfig = new JsonObject();
        discoveryConfig.add(COURSE_DISCOVERY, courseDiscoveryConfig);

        Config config = new Config(configBase);
        assertFalse(config.getDiscoveryConfig().getCourseDiscoveryConfig().isDiscoveryEnabled());
        assertFalse(config.getDiscoveryConfig().getCourseDiscoveryConfig().isWebviewDiscoveryEnabled());
        assertNull(config.getDiscoveryConfig().getCourseDiscoveryConfig().getBaseUrl());
        assertNull(config.getDiscoveryConfig().getCourseDiscoveryConfig().getInfoUrlTemplate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnrollmentInvalidType() {
        JsonObject configBase = new JsonObject();

        JsonObject discoveryConfig = new JsonObject();
        configBase.add(DISCOVERY, discoveryConfig);
        JsonObject courseDiscoveryConfig = new JsonObject();
        discoveryConfig.add(COURSE_DISCOVERY, courseDiscoveryConfig);
        courseDiscoveryConfig.add(TYPE, new JsonPrimitive("invalid type"));

        Config config = new Config(configBase);
        assertFalse(config.getDiscoveryConfig().getCourseDiscoveryConfig().isDiscoveryEnabled());
    }

    @RunWith(value = Parameterized.class)
    public static class EnrollmentConfigTests {

        private String course_enrollment_type;
        private boolean expected;

        public EnrollmentConfigTests(String course_enrollment_type, boolean expected) {
            this.course_enrollment_type = course_enrollment_type;
            this.expected = expected;
        }

        @Parameters(name = "{index}: willUseWebview({0})={1}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"webview", true},
                    {"WEBVIEW", true},
                    {"native", false},
                    {"NATIVE", false},
            });
        }

        @Test
        public void testEnrollmentConfig() {
            JsonObject configBase = new JsonObject();

            JsonObject discoveryConfig = new JsonObject();
            configBase.add(DISCOVERY, discoveryConfig);
            JsonObject courseDiscoveryConfig = new JsonObject();
            discoveryConfig.add(COURSE_DISCOVERY, courseDiscoveryConfig);

            JsonObject webviewConfig = new JsonObject();
            courseDiscoveryConfig.add(TYPE, new JsonPrimitive(course_enrollment_type));
            webviewConfig.add(BASE_URL, new JsonPrimitive("fake-url"));
            webviewConfig.add(DETAIL_TEMPLATE, new JsonPrimitive("fake-url-template"));
            courseDiscoveryConfig.add("WEBVIEW", webviewConfig);

            Config config = new Config(configBase);
            assertTrue(config.getDiscoveryConfig().getCourseDiscoveryConfig().isDiscoveryEnabled());
            assertEquals(config.getDiscoveryConfig().getCourseDiscoveryConfig().isWebviewDiscoveryEnabled(), expected);
            assertEquals(config.getDiscoveryConfig().getCourseDiscoveryConfig().getBaseUrl(), "fake-url");
            assertEquals(config.getDiscoveryConfig().getCourseDiscoveryConfig().getInfoUrlTemplate(), "fake-url-template");
        }
    }

    @Test
    public void testEnrollmentConfig_withExploreSubjectsEnabled() {
        JsonObject configBase = new JsonObject();

        JsonObject discoveryConfig = new JsonObject();
        configBase.add(DISCOVERY, discoveryConfig);
        JsonObject courseDiscoveryConfig = new JsonObject();

        discoveryConfig.add(COURSE_DISCOVERY, courseDiscoveryConfig);
        courseDiscoveryConfig.add(TYPE, new JsonPrimitive("invalid type"));

        JsonObject webviewConfig = new JsonObject();
        courseDiscoveryConfig.add(TYPE, new JsonPrimitive("WEBVIEW"));
        webviewConfig.add(BASE_URL, new JsonPrimitive("fake-url"));
        webviewConfig.add(EXPLORE_SUBJECTS_URL, new JsonPrimitive("explore-subjects-url"));
        webviewConfig.add(DETAIL_TEMPLATE, new JsonPrimitive("fake-url-template"));
        courseDiscoveryConfig.add("WEBVIEW", webviewConfig);

        Config config = new Config(configBase);
        assertTrue(config.getDiscoveryConfig().getCourseDiscoveryConfig().isDiscoveryEnabled());
        assertTrue(config.getDiscoveryConfig().getCourseDiscoveryConfig().isWebviewDiscoveryEnabled());
        assertEquals(config.getDiscoveryConfig().getCourseDiscoveryConfig().getBaseUrl(), "fake-url");
        assertEquals(config.getDiscoveryConfig().getCourseDiscoveryConfig().getInfoUrlTemplate(), "fake-url-template");
    }

    @Test
    public void testFacebookNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getFacebookConfig().isEnabled());
        assertNull(config.getFacebookConfig().getFacebookAppId());
    }

    @Test
    public void testFacebookEmptyConfig() {
        JsonObject fbConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(FACEBOOK, fbConfig);

        Config config = new Config(configBase);
        assertFalse(config.getFacebookConfig().isEnabled());
        assertNull(config.getFacebookConfig().getFacebookAppId());
    }

    @Test
    public void testFacebookConfig() {
        String appId = "fake-app-id";

        JsonObject fbConfig = new JsonObject();
        fbConfig.add(ENABLED, new JsonPrimitive(true));
        fbConfig.add(FACEBOOK_APP_ID, new JsonPrimitive(appId));

        JsonObject configBase = new JsonObject();
        configBase.add(FACEBOOK, fbConfig);

        Config config = new Config(configBase);
        assertTrue(config.getFacebookConfig().isEnabled());
        assertEquals(appId, config.getFacebookConfig().getFacebookAppId());
    }

    @Test
    public void testGoogleNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getGoogleConfig().isEnabled());
    }

    @Test
    public void testGoogleEmptyConfig() {
        JsonObject googleConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(GOOGLE, googleConfig);

        Config config = new Config(configBase);
        assertFalse(config.getGoogleConfig().isEnabled());
    }

    @Test
    public void testGoogleConfig() {
        JsonObject googleConfig = new JsonObject();
        googleConfig.add(ENABLED, new JsonPrimitive(true));

        JsonObject configBase = new JsonObject();
        configBase.add(GOOGLE, googleConfig);

        Config config = new Config(configBase);
        assertTrue(config.getGoogleConfig().isEnabled());
    }

    @Test
    public void testBranchNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getBranchConfig().isEnabled());
        assertNull(config.getBranchConfig().getKey());
        assertNull(config.getBranchConfig().getSecret());
    }

    @Test
    public void testBranchEmptyConfig() {
        JsonObject branchConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(BRANCH, branchConfig);

        Config config = new Config(configBase);
        assertFalse(config.getBranchConfig().isEnabled());
        assertNull(config.getBranchConfig().getKey());
        assertNull(config.getBranchConfig().getSecret());
    }

    @Test
    public void testBranchConfig() {
        final String key = "fake-key";
        final String secret = "fake-secret";

        JsonObject branchConfig = new JsonObject();
        branchConfig.add(ENABLED, new JsonPrimitive(true));
        branchConfig.add(KEY, new JsonPrimitive(key));
        branchConfig.add(SECRET, new JsonPrimitive(secret));

        JsonObject configBase = new JsonObject();
        configBase.add(BRANCH, branchConfig);

        Config config = new Config(configBase);
        assertTrue(config.getBranchConfig().isEnabled());
        assertEquals(key, config.getBranchConfig().getKey());
        assertEquals(secret, config.getBranchConfig().getSecret());
    }

    @Test
    public void testNewRelicNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getNewRelicConfig().isEnabled());
        assertNull(config.getNewRelicConfig().getNewRelicKey());
    }

    @Test
    public void testNewRelicEmptyConfig() {
        JsonObject fabricConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(NEW_RELIC, fabricConfig);

        Config config = new Config(configBase);
        assertFalse(config.getNewRelicConfig().isEnabled());
        assertNull(config.getNewRelicConfig().getNewRelicKey());
    }

    @Test
    public void testNewRelicConfig() {
        String key = "fake-key";

        JsonObject newRelicConfig = new JsonObject();
        newRelicConfig.add(ENABLED, new JsonPrimitive(true));
        newRelicConfig.add(NEW_RELIC_KEY, new JsonPrimitive(key));

        JsonObject configBase = new JsonObject();
        configBase.add(NEW_RELIC, newRelicConfig);

        Config config = new Config(configBase);
        assertTrue(config.getNewRelicConfig().isEnabled());
        assertEquals(key, config.getNewRelicConfig().getNewRelicKey());
    }

    @Test
    public void testSegmentNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getSegmentConfig().isEnabled());
        assertNull(config.getSegmentConfig().getSegmentWriteKey());
    }

    @Test
    public void testSegmentEmptyConfig() {
        JsonObject segmentConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(SEGMENT_IO, segmentConfig);

        Config config = new Config(configBase);
        assertFalse(config.getSegmentConfig().isEnabled());
        assertNull(config.getSegmentConfig().getSegmentWriteKey());
    }

    @Test
    public void testSegmentConfig() {
        String key = "fake-key";

        JsonObject segmentConfig = new JsonObject();
        segmentConfig.add(ENABLED, new JsonPrimitive(true));
        segmentConfig.add(SEGMENT_IO_WRITE_KEY, new JsonPrimitive(key));

        JsonObject configBase = new JsonObject();
        configBase.add(SEGMENT_IO, segmentConfig);

        Config config = new Config(configBase);
        assertTrue(config.getSegmentConfig().isEnabled());
        assertEquals(key, config.getSegmentConfig().getSegmentWriteKey());
    }
}
