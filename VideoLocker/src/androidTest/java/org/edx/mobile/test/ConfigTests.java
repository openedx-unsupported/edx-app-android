package org.edx.mobile.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.util.Config;

import java.util.ArrayList;

/**
 * Created by aleffert on 2/6/15.
 */
public class ConfigTests extends BaseTestCase {

    /* Config keys */
    private static final String COURSE_ENROLLMENT       = "COURSE_ENROLLMENT";
    private static final String SOCIAL_SHARING          = "SOCIAL_SHARING";
    private static final String ZERO_RATING             = "ZERO_RATING";
    private static final String FACEBOOK                = "FACEBOOK";
    private static final String GOOGLE                  = "GOOGLE";
    private static final String FABRIC                  = "FABRIC";
    private static final String NEW_RELIC               = "NEW_RELIC";
    private static final String SEGMENT_IO              = "SEGMENT_IO";

    private static final String ENABLED                 = "ENABLED";
    private static final String DISABLED_CARRIERS       = "DISABLED_CARRIERS";
    private static final String CARRIERS                = "CARRIERS";
    private static final String COURSE_SEARCH_URL       = "COURSE_SEARCH_URL";
    private static final String EXTERNAL_COURSE_SEARCH_URL = "EXTERNAL_COURSE_SEARCH_URL";
    private static final String COURSE_INFO_URL_TEMPLATE = "COURSE_INFO_URL_TEMPLATE";
    private static final String FACEBOOK_APP_ID         = "FACEBOOK_APP_ID";
    private static final String FABRIC_KEY              = "FABRIC_KEY";
    private static final String FABRIC_BUILD_SECRET     = "FABRIC_BUILD_SECRET";
    private static final String NEW_RELIC_KEY           = "NEW_RELIC_KEY";
    private static final String SEGMENT_IO_WRITE_KEY    = "SEGMENT_IO_WRITE_KEY";
    private static final String USE_DEPRECATED_REGISTRATION_API = "USE_DEPRECATED_REGISTRATION_API";

    public void testSocialSharingNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getSocialSharingConfig().isEnabled());
        assertEquals(config.getSocialSharingConfig().getDisabledCarriers().size(), 0);
    }

    public void testSocialSharingEmptyConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject socialConfig = new JsonObject();
        configBase.add(SOCIAL_SHARING, socialConfig);

        Config config = new Config(configBase);
        assertFalse(config.getSocialSharingConfig().isEnabled());
        assertEquals(config.getSocialSharingConfig().getDisabledCarriers().size(), 0);
    }

    public void testSocialSharingConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject socialConfig = new JsonObject();
        socialConfig.add(ENABLED, new JsonPrimitive(true));
        configBase.add(SOCIAL_SHARING, socialConfig);

        ArrayList<String> carrierList = new ArrayList<String>();
        carrierList.add("12345");
        carrierList.add("foo");
        JsonArray carriers = new JsonArray();
        for(String carrier : carrierList) {
            carriers.add(new JsonPrimitive(carrier));
        }
        socialConfig.add(DISABLED_CARRIERS, carriers);

        Config config = new Config(configBase);
        assertTrue(config.getSocialSharingConfig().isEnabled());
        assertEquals(carrierList, config.getSocialSharingConfig().getDisabledCarriers());
    }

    public void testZeroRatingNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getZeroRatingConfig().isEnabled());
        assertEquals(config.getZeroRatingConfig().getCarriers().size(), 0);
    }

    public void testZeroRatingEmptyConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject socialConfig = new JsonObject();
        configBase.add(ZERO_RATING, socialConfig);

        Config config = new Config(configBase);
        assertFalse(config.getZeroRatingConfig().isEnabled());
        assertEquals(config.getZeroRatingConfig().getCarriers().size(), 0);
    }

    public void testZeroRatingConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject zeroRatingConfig = new JsonObject();
        zeroRatingConfig.add(ENABLED, new JsonPrimitive(true));
        configBase.add(ZERO_RATING, zeroRatingConfig);

        ArrayList<String> carrierList = new ArrayList<String>();
        carrierList.add("12345");
        carrierList.add("foo");
        JsonArray carriers = new JsonArray();
        for(String carrier : carrierList) {
            carriers.add(new JsonPrimitive(carrier));
        }
        zeroRatingConfig.add(CARRIERS, carriers);

        Config config = new Config(configBase);
        assertTrue(config.getZeroRatingConfig().isEnabled());
        assertEquals(carrierList, config.getZeroRatingConfig().getCarriers());
    }

    public void testEnrollmentNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getEnrollmentConfig().isEnabled());
        assertNull(config.getEnrollmentConfig().getCourseSearchUrl());
        assertNull(config.getEnrollmentConfig().getExternalCourseSearchUrl());
        assertNull(config.getEnrollmentConfig().getCourseInfoUrlTemplate());
    }

    public void testEnrollmentEmptyConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject enrollmentConfig = new JsonObject();
        configBase.add(COURSE_ENROLLMENT, enrollmentConfig);

        Config config = new Config(configBase);
        assertFalse(config.getEnrollmentConfig().isEnabled());
        assertNull(config.getEnrollmentConfig().getCourseSearchUrl());
        assertNull(config.getEnrollmentConfig().getExternalCourseSearchUrl());
        assertNull(config.getEnrollmentConfig().getCourseInfoUrlTemplate());
    }

    public void testEnrollmentConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject enrollmentConfig = new JsonObject();
        enrollmentConfig.add(ENABLED, new JsonPrimitive(true));
        enrollmentConfig.add(COURSE_SEARCH_URL, new JsonPrimitive("fake-url"));
        enrollmentConfig.add(EXTERNAL_COURSE_SEARCH_URL, new JsonPrimitive("external-fake-url"));
        enrollmentConfig.add(COURSE_INFO_URL_TEMPLATE, new JsonPrimitive("fake-url-template"));
        configBase.add(COURSE_ENROLLMENT, enrollmentConfig);

        Config config = new Config(configBase);
        assertTrue(config.getEnrollmentConfig().isEnabled());
        assertEquals(config.getEnrollmentConfig().getCourseSearchUrl(), "fake-url");
        assertEquals(config.getEnrollmentConfig().getExternalCourseSearchUrl(), "external-fake-url");
        assertEquals(config.getEnrollmentConfig().getCourseInfoUrlTemplate(), "fake-url-template");
    }

    public void testFacebookNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getFacebookConfig().isEnabled());
        assertNull(config.getFacebookConfig().getFacebookAppId());
    }

    public void testFacebookEmptyConfig() {
        JsonObject fbConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(FACEBOOK, fbConfig);

        Config config = new Config(configBase);
        assertFalse(config.getFacebookConfig().isEnabled());
        assertNull(config.getFacebookConfig().getFacebookAppId());
    }

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

    public void testGoogleNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getGoogleConfig().isEnabled());
    }

    public void testGoogleEmptyConfig() {
        JsonObject googleConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(GOOGLE, googleConfig);

        Config config = new Config(configBase);
        assertFalse(config.getGoogleConfig().isEnabled());
    }

    public void testGoogleConfig() {
        JsonObject googleConfig = new JsonObject();
        googleConfig.add(ENABLED, new JsonPrimitive(true));

        JsonObject configBase = new JsonObject();
        configBase.add(GOOGLE, googleConfig);

        Config config = new Config(configBase);
        assertTrue(config.getGoogleConfig().isEnabled());
    }

    public void testFabricNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getFabricConfig().isEnabled());
        assertNull(config.getFabricConfig().getFabricKey());
        assertNull(config.getFabricConfig().getFabricBuildSecret());
    }

    public void testFabricEmptyConfig() {
        JsonObject fabricConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(FABRIC, fabricConfig);

        Config config = new Config(configBase);
        assertFalse(config.getFabricConfig().isEnabled());
        assertNull(config.getFabricConfig().getFabricKey());
        assertNull(config.getFabricConfig().getFabricBuildSecret());
    }

    public void testFabricConfig() {
        String key = "fake-key";
        String secret = "fake-secret";

        JsonObject fabricConfig = new JsonObject();
        fabricConfig.add(ENABLED, new JsonPrimitive(true));
        fabricConfig.add(FABRIC_KEY, new JsonPrimitive(key));
        fabricConfig.add(FABRIC_BUILD_SECRET, new JsonPrimitive(secret));

        JsonObject configBase = new JsonObject();
        configBase.add(FABRIC, fabricConfig);

        Config config = new Config(configBase);
        assertTrue(config.getFabricConfig().isEnabled());
        assertEquals(key, config.getFabricConfig().getFabricKey());
        assertEquals(secret, config.getFabricConfig().getFabricBuildSecret());
    }

    public void testNewRelicNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getNewRelicConfig().isEnabled());
        assertNull(config.getNewRelicConfig().getNewRelicKey());
    }

    public void testNewRelicEmptyConfig() {
        JsonObject fabricConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(NEW_RELIC, fabricConfig);

        Config config = new Config(configBase);
        assertFalse(config.getNewRelicConfig().isEnabled());
        assertNull(config.getNewRelicConfig().getNewRelicKey());
    }

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

    public void testSegmentNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getSegmentConfig().isEnabled());
        assertNull(config.getSegmentConfig().getSegmentWriteKey());
    }

    public void testSegmentEmptyConfig() {
        JsonObject segmentConfig = new JsonObject();

        JsonObject configBase = new JsonObject();
        configBase.add(SEGMENT_IO, segmentConfig);

        Config config = new Config(configBase);
        assertFalse(config.getSegmentConfig().isEnabled());
        assertNull(config.getSegmentConfig().getSegmentWriteKey());
    }

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
