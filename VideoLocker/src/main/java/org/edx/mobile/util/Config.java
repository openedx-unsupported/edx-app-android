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
    private static final String FABRIC_KEY = "FABRIC_KEY";
    private static final String ENVIRONMENT_DISPLAY_NAME = "ENVIRONMENT_DISPLAY_NAME";
    private static final String FACEBOOK_APP_ID = "FACEBOOK_APP_ID";
    private static final String FEEDBACK_EMAIL_ADDRESS = "FEEDBACK_EMAIL_ADDRESS";
    private static final String GOOGLE_PLUS_KEY = "GOOGLE_PLUS_KEY";
    private static final String OAUTH_CLIENT_SECRET = "OAUTH_CLIENT_SECRET";
    private static final String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
    private static final String SEGMENT_IO_WRITE_KEY = "SEGMENT_IO_WRITE_KEY";
    private static final String NEW_RELIC_KEY = "NEW_RELIC_KEY";
    private static final String SOCIAL_SHARING = "SOCIAL_SHARING";
    private static final String ZERO_RATING = "ZERO_RATING";
    private static final String COURSE_ENROLLMENT = "COURSE_ENROLLMENT";
    private static final String THIRD_PARTY_TRAFFIC = "THIRD_PARTY_TRAFFIC";
    private static final String REGISTRATION_API_DEPRECATED = "REGISTRATION_API_DEPRECATED";

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

    public class EnrollmentConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("SEARCH_URL") String mSearchUrl;
        private @SerializedName("COURSE_INFO_URL_TEMPLATE") String mCourseInfoUrlTemplate;

        public boolean getEnabled() {
            return mEnabled;
        }

        public String getSearchUrl() {
            return mSearchUrl;
        }

        public String getCourseInfoUrlTemplate() {
            return mCourseInfoUrlTemplate;
        }
    }

    /**
     * Third party service calls are enabled by default, if no configuration is provided.
     * Providing empty configuration disables all the services.
     * Providing complete configuration allows to enable or disable the calls to individual service.
     */
    public class ThirdPartyTrafficConfig {
        private @SerializedName("GOOGLE_ENABLED") boolean googleEnabled = true;
        private @SerializedName("FACEBOOK_ENABLED") boolean facebookEnabled = true;
        private @SerializedName("NEW_RELIC_ENABLED") boolean newRelicEnabled = true;
        private @SerializedName("FABRIC_ENABLED") boolean fabricEnabled = true;
        private @SerializedName("SEGMENTIO_ENABLED") boolean segmentEnabled = true;

        public boolean isGoogleEnabled() {
            return googleEnabled;
        }

        public boolean isFacebookEnabled() {
            return facebookEnabled;
        }

        public boolean isNewRelicEnabled() {
            return newRelicEnabled;
        }

        public boolean isFabricEnabled() {
            return fabricEnabled;
        }

        public boolean isSegmentEnabled() {
            return segmentEnabled;
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

    public boolean isRegistrationAPIDeprecated() {
        return getBoolean(REGISTRATION_API_DEPRECATED, true);
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

    /**
     * Returns configuration for Enrollments.
     * @return
     */
    public EnrollmentConfig getEnrollment() {
        JsonElement element = getObject(COURSE_ENROLLMENT);
        if(element != null) {
            Gson gson = new Gson();
            EnrollmentConfig config = gson.fromJson(element, EnrollmentConfig.class);
            return config;
        }
        else {
            return new EnrollmentConfig();
        }
    }

    /**
     * Returns configuration for third party traffic (network requests).
     * @return
     */
    public ThirdPartyTrafficConfig getThirdPartyTraffic() {
        JsonElement element = getObject(THIRD_PARTY_TRAFFIC);
        if(element != null) {
            Gson gson = new Gson();
            ThirdPartyTrafficConfig config = gson.fromJson(element, ThirdPartyTrafficConfig.class);
            return config;
        }
        else {
            return new ThirdPartyTrafficConfig();
        }
    }
}