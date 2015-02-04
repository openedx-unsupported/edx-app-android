package org.edx.mobile.util;

import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.io.IOUtils;
import org.edx.mobile.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

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
    private JSONObject mProperties;

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
    private static final String SOCIAL_FEATURES_ENABLED = "SOCIAL_FEATURES_ENABLED";

    Config(Context context) {
        try {
            InputStream in = context.getAssets().open("config/config.json");
            String strConfig = IOUtils.toString(in);
            mProperties = new JSONObject(strConfig);
        } catch (Exception e) {
            mProperties = new JSONObject();
            logger.error(e);
        }
    }

    Config(JSONObject properties) {
        mProperties = properties;
    }

    String getString(String key) {
        try {
            return mProperties.getString(key);
        } catch (JSONException e) {
            logger.error(e);
        }
        return null;
    }

    boolean getBoolean(String key, boolean defaultValue) {
        Boolean isTrue = null;
        try {
            isTrue = (Boolean)mProperties.get(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return defaultValue;
        }
        return isTrue == null ? defaultValue : isTrue;
    }

    private int getInteger(String key, int defaultValue) {
        Integer value = null;
        try {
            value = (Integer)mProperties.get(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return defaultValue;
        }
        return value == null ? defaultValue : value;
    }

    private Object getObject(String key) {
        try {
            return mProperties.get(key);
        } catch (JSONException e) {
            logger.error(e);
        }
        return null;
    }


    /// Known Configurations
    /// Create methods instead of just using the key names directly for a little extra flexibility

    public String getApiHostURL() {
        return getString(API_HOST_URL);
    }

    public String getCourseSearchUrl() {
        return getString(COURSE_SEARCH_URL);
    }

    public String getFabricKey() {
        return getString(FABRIC_KEY);
    }

    public String getFeedbackEmailAddress() {
        return getString(FEEDBACK_EMAIL_ADDRESS);
    }

    public String getGooglePlusKey() {
        return getString(GOOGLE_PLUS_KEY);
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

    public String getNewRelicKey() {
        return getString(NEW_RELIC_KEY);
    }

    public String getFacebookAppId() {
        return getString(FACEBOOK_APP_ID);
    }

    public String getEnvironmentDisplayName() {
        return getString(ENVIRONMENT_DISPLAY_NAME);
    }
    public boolean getSocialFeaturesEnabled() {
        return getBoolean(SOCIAL_FEATURES_ENABLED, false);
    }
}
