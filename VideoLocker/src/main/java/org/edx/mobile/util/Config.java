package org.edx.mobile.util;

import android.content.Context;
import android.text.TextUtils;

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

    /* Individual configuration keys */
    private static final String API_HOST_URL = "API_HOST_URL";
    private static final String ENVIRONMENT_DISPLAY_NAME = "ENVIRONMENT_DISPLAY_NAME";
    private static final String FEEDBACK_EMAIL_ADDRESS = "FEEDBACK_EMAIL_ADDRESS";
    private static final String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
    private static final String OAUTH_CLIENT_SECRET = "OAUTH_CLIENT_SECRET";
    private static final String USE_DEPRECATED_REGISTRATION_API = "USE_DEPRECATED_REGISTRATION_API";

    /* Composite configuration keys */
    private static final String COURSE_ENROLLMENT = "COURSE_ENROLLMENT";
    private static final String SOCIAL_SHARING = "SOCIAL_SHARING";
    private static final String ZERO_RATING = "ZERO_RATING";
    private static final String FACEBOOK = "FACEBOOK";
    private static final String GOOGLE = "GOOGLE";
    private static final String FABRIC = "FABRIC";
    private static final String NEW_RELIC = "NEW_RELIC";
    private static final String SEGMENT_IO = "SEGMENT_IO";

    /**
     * Social Sharing configuration.
     */
    public class SocialSharingConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("DISABLED_CARRIERS") List<String> mDisabledCarriers;

        public boolean isEnabled() {
            return mEnabled;
        }

        public List<String> getDisabledCarriers() {
            return mDisabledCarriers != null ? mDisabledCarriers : new ArrayList<String>();
        }
    }

    /**
     * Zero Rating configuration.
     */
    public class ZeroRatingConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("CARRIERS") List<String> mCarriers;

        public boolean isEnabled() {
            return mEnabled;
        }

        public List<String> getCarriers() {
            return mCarriers != null ? mCarriers : new ArrayList<String>();
        }
    }

    /**
     * Course Enrollment configuration.
     */
    public class EnrollmentConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("COURSE_SEARCH_URL") String mSearchUrl;
        private @SerializedName("EXTERNAL_COURSE_SEARCH_URL") String mExternalSearchUrl;
        private @SerializedName("COURSE_INFO_URL_TEMPLATE") String mCourseInfoUrlTemplate;

        public boolean isEnabled() {
            return mEnabled;
        }

        public String getCourseSearchUrl() {
            return mSearchUrl;
        }

        public String getExternalCourseSearchUrl() {
            return mExternalSearchUrl;
        }

        public String getCourseInfoUrlTemplate() {
            return mCourseInfoUrlTemplate;
        }
    }

    /**
     * Facebook configuration.
     */
    public class FacebookConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("FACEBOOK_APP_ID") String mFacebookAppId;

        public boolean isEnabled() {
            return mEnabled && !TextUtils.isEmpty(mFacebookAppId);
        }

        public String getFacebookAppId() {
            return mFacebookAppId;
        }
    }

    /**
     * Google configuration.
     */
    public class GoogleConfig {
        private @SerializedName("ENABLED") boolean mEnabled;

        public boolean isEnabled() {
            return mEnabled;
        }
    }

    /**
     * Fabric configuration.
     */
    public class FabricConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("FABRIC_KEY") String mFabricKey;
        private @SerializedName("FABRIC_BUILD_SECRET") String mFabricBuildSecret;

        public boolean isEnabled() {
            return mEnabled
                    && !TextUtils.isEmpty(mFabricKey)
                    && !TextUtils.isEmpty(mFabricBuildSecret);
        }

        public String getFabricKey() {
            return mFabricKey;
        }

        public String getFabricBuildSecret() {
            return mFabricBuildSecret;
        }
    }

    /**
     * New Relic configuration.
     */
    public class NewRelicConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("NEW_RELIC_KEY") String mNewRelicKey;

        public boolean isEnabled() {
            return mEnabled && !TextUtils.isEmpty(mNewRelicKey);
        }

        public String getNewRelicKey() {
            return mNewRelicKey;
        }
    }

    /**
     * SegmentIO configuration.
     */
    public class SegmentConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("SEGMENT_IO_WRITE_KEY") String mSegmentWriteKey;

        public boolean isEnabled() {
            return mEnabled && !TextUtils.isEmpty(mSegmentWriteKey);
        }

        public String getSegmentWriteKey() {
            return mSegmentWriteKey;
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

    private String getString(String key) {
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

    public String getFeedbackEmailAddress() {
        return getString(FEEDBACK_EMAIL_ADDRESS);
    }

    public String getOAuthClientId() {
        return getString(OAUTH_CLIENT_ID);
    }

    public String getOAuthClientSecret() {
        return getString(OAUTH_CLIENT_SECRET);
    }

    public boolean isUseDeprecatedRegistrationAPI() {
        return getBoolean(USE_DEPRECATED_REGISTRATION_API, false);
    }

    /**
     * Returns Course Enrollment configuration.
     * @return
     */
    public EnrollmentConfig getEnrollmentConfig() {
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
     * Returns Social Sharing configuration.
     * @return
     */
    public SocialSharingConfig getSocialSharingConfig() {
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

    /**
     * Returns Zero Rating configuration.
     * @return
     */
    public ZeroRatingConfig getZeroRatingConfig() {
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

    public FacebookConfig getFacebookConfig() {
        JsonElement element = getObject(FACEBOOK);
        if(element != null) {
            Gson gson = new Gson();
            FacebookConfig config = gson.fromJson(element, FacebookConfig.class);
            return config;
        }
        else {
            return new FacebookConfig();
        }
    }

    public GoogleConfig getGoogleConfig() {
        JsonElement element = getObject(GOOGLE);
        if(element != null) {
            Gson gson = new Gson();
            GoogleConfig config = gson.fromJson(element, GoogleConfig.class);
            return config;
        }
        else {
            return new GoogleConfig();
        }
    }

    public FabricConfig getFabricConfig() {
        JsonElement element = getObject(FABRIC);
        if(element != null) {
            Gson gson = new Gson();
            FabricConfig config = gson.fromJson(element, FabricConfig.class);
            return config;
        }
        else {
            return new FabricConfig();
        }
    }

    public NewRelicConfig getNewRelicConfig() {
        JsonElement element = getObject(NEW_RELIC);
        if(element != null) {
            Gson gson = new Gson();
            NewRelicConfig config = gson.fromJson(element, NewRelicConfig.class);
            return config;
        }
        else {
            return new NewRelicConfig();
        }
    }

    public SegmentConfig getSegmentConfig() {
        JsonElement element = getObject(SEGMENT_IO);
        if(element != null) {
            Gson gson = new Gson();
            SegmentConfig config = gson.fromJson(element, SegmentConfig.class);
            return config;
        }
        else {
            return new SegmentConfig();
        }
    }
}