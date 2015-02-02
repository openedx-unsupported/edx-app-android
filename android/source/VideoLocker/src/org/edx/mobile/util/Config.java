package org.edx.mobile.util;

import android.content.Context;

import org.edx.mobile.logger.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleffert on 1/8/15.
 */
public class Config {

    protected final Logger logger = new Logger(getClass().getName());
    Map<String, Object> mProperties;

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
    private static final String NEW_RELIC_KEY = "NEW_RELIC_KEY";

    Config(Context context) {
        Yaml yaml = new Yaml();
        try {
            mProperties = (Map<String, Object>)yaml.load(context.getAssets().open("config/config.yaml"));
        } catch (IOException e) {
            mProperties = new HashMap<String, Object>();
            logger.error(e);
        }
    }

    Config(Map<String, Object> properties) {
        mProperties = properties;
    }

    String getString(String key) {
        return (String)mProperties.get(key);
    }

    private Object getObject(String key) {
        return mProperties.get(key);
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

    public String getNewRelicKey() {
        return getString(NEW_RELIC_KEY);
    }

    public String getFacebookAppId() {
        return getString(FACEBOOK_APP_ID);
    }

    public String getEnvironmentDisplayName() {
        return getString(ENVIRONMENT_DISPLAY_NAME);
    }
}
