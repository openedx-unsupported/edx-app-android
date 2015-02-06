package org.edx.mobile.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.edx.mobile.util.Config;

import java.util.ArrayList;

/**
 * Created by aleffert on 2/6/15.
 */
public class ConfigTests extends BaseTestCase {
    private static final String ENABLED = "ENABLED";

    private static final String SOCIAL_SHARING = "SOCIAL_SHARING";
    private static final String DISABLED_CARRIERS = "DISABLED_CARRIERS";

    private static final String ZERO_RATING = "ZERO_RATING";
    private static final String CARRIERS = "CARRIERS";

    public void testSocialSharingNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getSocialSharing().getEnabled());
        assertEquals(config.getSocialSharing().getDisabledCarriers().size(), 0);
    }

    public void testSocialSharingEmptyConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject socialConfig = new JsonObject();
        configBase.add(SOCIAL_SHARING, socialConfig);
        Config config = new Config(configBase);
        assertFalse(config.getSocialSharing().getEnabled());
        assertEquals(config.getSocialSharing().getDisabledCarriers().size(), 0);
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
        assertTrue(config.getSocialSharing().getEnabled());
        assertEquals(carrierList, config.getSocialSharing().getDisabledCarriers());
    }

    public void testZeroRatingNoConfig() {
        JsonObject configBase = new JsonObject();
        Config config = new Config(configBase);
        assertFalse(config.getZeroRating().getEnabled());
        assertEquals(config.getZeroRating().getCarriers().size(), 0);
    }

    public void testZeroRatingEmptyConfig() {
        JsonObject configBase = new JsonObject();
        JsonObject socialConfig = new JsonObject();
        configBase.add(ZERO_RATING, socialConfig);
        Config config = new Config(configBase);
        assertFalse(config.getZeroRating().getEnabled());
        assertEquals(config.getZeroRating().getCarriers().size(), 0);
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
        assertTrue(config.getZeroRating().getEnabled());
        assertEquals(carrierList, config.getZeroRating().getCarriers());
    }
}
