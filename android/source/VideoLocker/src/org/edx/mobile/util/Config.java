package org.edx.mobile.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import org.edx.mobile.logger.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleffert on 1/8/15.
 */
public class Config {
    private static Config sInstance;

    // Note that this is not thread safe. The expectation is that this only happens
    // immediately when the app launches or synchronously at the start of a test.
    public static void setInstance(Config config) {
        sInstance = config;
    }

    public static Config getInstance() {
        return sInstance;
    }

    protected final Logger logger = new Logger(getClass().getName());
    private JsonObject mProperties;

    private static final String API_HOST_URL = "API_HOST_URL";
    private static final String COURSE_SEARCH_URL = "COURSE_SEARCH_URL";
    private static final String FABRIC_KEY = "FABRIC_KEY";
    private static final String ENVIRONMENT_DISPLAY_NAME = "ENVIRONMENT_DISPLAY_NAME";
    private static final String FACEBOOK_APP_ID = "FACEBOOK_APP_ID";
    private static final String FEEDBACK_EMAIL_ADDRESS = "FEEDBACK_EMAIL_ADDRESS";
    private static final String GOOGLE_PLUS_KEY = "GOOGLE_PLUS_KEY";
    private static final String OAUTH_CLIENT_SECRET = "OAUTH_CLIENT_SECRET";
    private static final String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
    private static final String SEGMENT_IO_WRITE_KEY = "SEGMENT_IO_WRITE_KEY";
    private static final String SEGMENT_IO_DEBUG_ON = "SEGMENT_IO_DEBUG_ON";
    private static final String SEGMENT_IO_QUEUE_SIZE = "SEGMENT_IO_QUEUE_SIZE";
    private static final String NEW_RELIC_KEY = "NEW_RELIC_KEY";
    private static final String SOCIAL_SHARING = "SOCIAL_SHARING";
    private static final String ZERO_RATING = "ZERO_RATING";

    public class SocialSharingConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("DISABLED_CARRIERS") List<String> mDisabledCarriers;

        public boolean getEnabled() {
            return mEnabled;
        }

        public List<String> getDisabledCarriers() {
            return mDisabledCarriers != null ? mDisabledCarriers : new ArrayList<String>();
        }
    }

    public class ZeroRatingConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("CARRIERS") List<String> mCarriers;

        public boolean getEnabled() {
            return mEnabled;
        }

        public List<String> getCarriers() {
            return mCarriers != null ? mCarriers : new ArrayList<String>();
        }
    }

    Config(Context context) {
        try {
            InputStream in = context.getAssets().open("config/config.json");
            JsonParser parser = new JsonParser();
            JsonElement config = parser.parse(new InputStreamReader(in));
            mProperties = config.getAsJsonObject();
        } catch (Exception e) {
            mProperties = new JsonObject();
            logger.error(e);
        }
    }

    public Config(JsonObject properties) {
        mProperties = properties;
    }

    String getString(String key) {
        return getString(key, null);
    }

    private String getString(String key, String defaultValue) {
        JsonElement element = getObject(key);
        if(element != null) {
            return element.getAsString();
        }
        else {
            return defaultValue;
        }
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        JsonElement element = getObject(key);
        if(element != null) {
            return element.getAsBoolean();
        }
        else {
            return defaultValue;
        }
    }

    private int getInteger(String key, int defaultValue) {
        JsonElement element = getObject(key);
        if(element != null) {
            return element.getAsInt();
        }
        else {
            return defaultValue;
        }
    }

    private JsonElement getObject(String key) {
        return mProperties.get(key);
    }


    /// Known Configurations
    /// Create methods instead of just using the key names directly for a little extra flexibility
    /// Please keep this alphabetized

    public String getApiHostURL() {
        return getString(API_HOST_URL);
    }

    public String getCourseSearchUrl() {
        return getString(COURSE_SEARCH_URL);
    }

    public String getEnvironmentDisplayName() {
        return getString(ENVIRONMENT_DISPLAY_NAME);
    }

    public String getFabricKey() {
        return getString(FABRIC_KEY);
    }

    public String getFacebookAppId() {
        return getString(FACEBOOK_APP_ID);
    }

    public String getFeedbackEmailAddress() {
        return getString(FEEDBACK_EMAIL_ADDRESS);
    }

    public String getGooglePlusKey() {
        return getString(GOOGLE_PLUS_KEY);
    }

    public String getNewRelicKey() {
        return getString(NEW_RELIC_KEY);
    }

    public String getOAuthClientId() {
        return getString(OAUTH_CLIENT_ID);
    }

    public String getOAuthClientSecret() {
        return getString(OAUTH_CLIENT_SECRET);
    }

    public String getSegmentIOWriteKey() {
        return getString(SEGMENT_IO_WRITE_KEY);
    }

    public boolean getSegmentDebugOn() {
        return getBoolean(SEGMENT_IO_DEBUG_ON, false);
    }

    public int getSegmentQueueSize() {
        return getInteger(SEGMENT_IO_QUEUE_SIZE, 1);
    }

    public SocialSharingConfig getSocialSharing() {
        JsonElement element = getObject(SOCIAL_SHARING);
        if(element != null) {
            Gson gson = new Gson();
            SocialSharingConfig config = gson.fromJson(element, SocialSharingConfig.class);
            return config;
        }
        else {
            return new SocialSharingConfig();
        }
    }

    public ZeroRatingConfig getZeroRating() {
        JsonElement element = getObject(ZERO_RATING);
        if(element != null) {
            Gson gson = new Gson();
            ZeroRatingConfig config = gson.fromJson(element, ZeroRatingConfig.class);
            return config;
        }
        else {
            return new ZeroRatingConfig();
        }
    }
}
