package org.edx.mobile.util;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by aleffert on 1/8/15.
 */
@Singleton
public class Config {

    private static final Logger logger = new Logger(Config.class.getName());
    private JsonObject mProperties;

    /* Individual configuration keys */
    private static final String API_HOST_URL = "API_HOST_URL";
    private static final String ENVIRONMENT_DISPLAY_NAME = "ENVIRONMENT_DISPLAY_NAME";
    private static final String PLATFORM_NAME = "PLATFORM_NAME";
    private static final String PLATFORM_DESTINATION_NAME = "PLATFORM_DESTINATION_NAME";
    private static final String FEEDBACK_EMAIL_ADDRESS = "FEEDBACK_EMAIL_ADDRESS";
    private static final String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
    private static final String SPEED_TEST_ENABLED = "SPEED_TEST_ENABLED";

    /* Composite configuration keys */
    private static final String COURSE_ENROLLMENT = "COURSE_ENROLLMENT";
    private static final String SOCIAL_SHARING = "SOCIAL_SHARING";
    private static final String ZERO_RATING = "ZERO_RATING";
    private static final String FACEBOOK = "FACEBOOK";
    private static final String GOOGLE = "GOOGLE";
    private static final String TWITTER = "TWITTER";
    private static final String FABRIC = "FABRIC";
    private static final String NEW_RELIC = "NEW_RELIC";
    private static final String SEGMENT_IO = "SEGMENT_IO";
    private static final String PUSH_NOTIFICATIONS_FLAG = "PUSH_NOTIFICATIONS";
    private static final String PARSE = "PARSE";
    private static final String WHITE_LIST_OF_DOMAINS = "WHITE_LIST_OF_DOMAINS";

    private static final String PARSE_APPLICATION_ID = "APPLICATION_ID";
    private static final String PARSE_CLIENT_KEY = "CLIENT_KEY";

    private static final String TEST_ACCOUNT = "TEST_ACCOUNT";
    public static final String TEST_ACCOUNT_NAME = "NAME";
    public static final String TEST_ACCOUNT_PASSWORD = "PASSWORD";

    public static final String USER_PROFILES_ENABLED = "USER_PROFILES_ENABLED";
    public static final String DISCUSSIONS_ENABLED = "DISCUSSIONS_ENABLED";

    public static final String CERTIFICATES_ENABLED = "CERTIFICATES_ENABLED";

    public static final String COURSE_SHARING_ENABLED = "COURSE_SHARING_ENABLED";

    private static final String SERVER_SIDE_CHANGED_THREAD = "SERVER_SIDE_CHANGED_THREAD";

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
        private @SerializedName("WHITE_LIST_OF_DOMAINS") List<String> mWhiteListedDomains;

        public boolean isEnabled() {
            return mEnabled;
        }

        public List<String> getCarriers() {
            return mCarriers != null ? mCarriers : new ArrayList<String>();
        }

        public List<String> getWhiteListedDomains() {
            return mWhiteListedDomains != null ? mWhiteListedDomains : new ArrayList<String>();
        }
    }

    /**
     * Course Enrollment configuration.
     *
     * If TYPE is not "webview" in any letter case, defaults to "native"
     */
    public class EnrollmentConfig {
        private @SerializedName("WEBVIEW") WebViewConfig mWebViewConfig;
        private @SerializedName("TYPE") String mCourseEnrollmentType;

        public boolean isWebviewCourseDiscoveryEnabled() {
            if (mCourseEnrollmentType == null) { return false; }

            switch (mCourseEnrollmentType.toUpperCase(Locale.US)) {
                case "WEBVIEW":
                    return true;
                case "NATIVE":
                    return false;
                default:
                    logger.debug("No match in config for COURSE_ENROLLMENT.TYPE:" + mCourseEnrollmentType + ". Defaulting to Native");
                    return false;
            }
    }

        public String getCourseSearchUrl() {
            return mWebViewConfig.getCourseSearchUrl();
        }

        public String getCourseInfoUrlTemplate() {
            return mWebViewConfig.getCourseInfoUrlTemplate();
        }
    }

    public static class WebViewConfig {
        private @SerializedName("COURSE_SEARCH_URL") String mSearchUrl;
        private @SerializedName("COURSE_INFO_URL_TEMPLATE") String mCourseInfoUrlTemplate;

        public String getCourseSearchUrl() {
            return mSearchUrl;
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
     * Twitter configuration.
     */
    public class TwitterConfig {
        private @SerializedName("HASHTAG") String mHashTag;

        public String getHashTag() {
            return mHashTag;
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
     * Testing account - we may need a better solution in the future.
     */
    public class TestAccountConfig {
        private @SerializedName("NAME") String mName;
        private @SerializedName("PASSWORD") String mPassword;

        public String getName() { return mName; }
        public String getPassword() { return mPassword; }
    }

    /**
     * Parse Notification
     */
    public class ParseNotificationConfig {
        private @SerializedName("NOTIFICATIONS_ENABLED") boolean mEnabled;
        private @SerializedName("APPLICATION_ID") String mParseApplicationId;
        private @SerializedName("CLIENT_KEY") String mParseClientKey;

        public boolean isEnabled() {
            return mEnabled && !TextUtils.isEmpty(mParseClientKey);
        }

        public String getParseApplicationId() { return mParseApplicationId; }

        public String getParseClientKey() {
            return mParseClientKey;
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

    /**
     * Domain White List configuration.
     */
    public class DomainWhiteListConfig {
        private @SerializedName("ENABLED") boolean mEnabled;
        private @SerializedName("DOMAINS") List<String> mDomains;

        public boolean isEnabled() {
            return mEnabled;
        }

        public List<String> getDomains() {
            return mDomains != null ? mDomains : new ArrayList<String>();
        }
    }

    @Inject
    public Config(Context context) {
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

    // User facing name of the platform like "edX"
    public String getPlatformName() {
        return getString(PLATFORM_NAME);
    }

    // User facing name of the platform as a destination like "edx.org"
    public String getPlatformDestinationName() {
        return getString(PLATFORM_DESTINATION_NAME);
    }

    // Debug name of the current configuration
    public String getEnvironmentDisplayName() {
        return getString(ENVIRONMENT_DISPLAY_NAME);
    }

    public String getFeedbackEmailAddress() {
        return getString(FEEDBACK_EMAIL_ADDRESS);
    }

    public String getOAuthClientId() {
        return getString(OAUTH_CLIENT_ID);
    }

    public boolean isNotificationEnabled() {
        return getBoolean(PUSH_NOTIFICATIONS_FLAG, false);
    }

    /**
     * Empty or no config returns false.
     * Otherwise, returns the value from the config.
     * @return
     */
    public boolean isSpeedTestEnabled() {
        return getBoolean(SPEED_TEST_ENABLED, false);
    }

    public boolean isUserProfilesEnabled() {
        return getBoolean(USER_PROFILES_ENABLED, false);
    }

    public boolean isDiscussionsEnabled() {
        return getBoolean(DISCUSSIONS_ENABLED, false);
    }

    public boolean areCertificateLinksEnabled() { return getBoolean(CERTIFICATES_ENABLED, false); }

    public boolean isCourseSharingEnabled() { return getBoolean(COURSE_SHARING_ENABLED, false); }

    public EnrollmentConfig getCourseDiscoveryConfig() {
        JsonElement element = getObject(COURSE_ENROLLMENT);

        if(element != null) {
            return new Gson().fromJson(element, EnrollmentConfig.class);
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

    public TwitterConfig getTwitterConfig() {
        JsonElement element = getObject(TWITTER);
        if(element != null) {
            Gson gson = new Gson();
            TwitterConfig config = gson.fromJson(element, TwitterConfig.class);
            return config;
        }
        else {
            return new TwitterConfig();
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

    public ParseNotificationConfig getParseNotificationConfig() {
        JsonElement element = getObject(PARSE);
        if(element != null) {
            Gson gson = new Gson();
            ParseNotificationConfig config = gson.fromJson(element, ParseNotificationConfig.class);
            return config;
        }
        else {
            return new ParseNotificationConfig();
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

    public TestAccountConfig getTestAccountConfig() {
        JsonElement element = getObject(TEST_ACCOUNT);
        if(element != null) {
            Gson gson = new Gson();
            TestAccountConfig config = gson.fromJson(element, TestAccountConfig.class);
            return config;
        }
        else {
            return new TestAccountConfig();
        }
    }
}
