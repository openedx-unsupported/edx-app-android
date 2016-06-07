package org.edx.mobile.util;

import android.content.Context;
import android.support.annotation.NonNull;
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
    private static final String ZERO_RATING = "ZERO_RATING";
    private static final String FACEBOOK = "FACEBOOK";
    private static final String GOOGLE = "GOOGLE";
    private static final String TWITTER = "TWITTER";
    private static final String FABRIC = "FABRIC";
    private static final String NEW_RELIC = "NEW_RELIC";
    private static final String SEGMENT_IO = "SEGMENT_IO";
    private static final String PUSH_NOTIFICATIONS_FLAG = "PUSH_NOTIFICATIONS";
    private static final String WHITE_LIST_OF_DOMAINS = "WHITE_LIST_OF_DOMAINS";

    private static final String TEST_ACCOUNT = "TEST_ACCOUNT";
    private static final String TEST_ACCOUNT_NAME = "NAME";
    private static final String TEST_ACCOUNT_PASSWORD = "PASSWORD";

    // Features
    private static final String USER_PROFILES_ENABLED = "USER_PROFILES_ENABLED";
    private static final String DISCUSSIONS_ENABLED = "DISCUSSIONS_ENABLED";
    private static final String CERTIFICATES_ENABLED = "CERTIFICATES_ENABLED";
    private static final String COURSE_SHARING_ENABLED = "COURSE_SHARING_ENABLED";
    private static final String BADGES_ENABLED = "BADGES_ENABLED";
    private static final String SERVER_SIDE_CHANGED_THREAD = "SERVER_SIDE_CHANGED_THREAD";
    private static final String END_TO_END_TEST = "END_TO_END_TEST";

    public static class ZeroRatingConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("CARRIERS")
        private List<String> mCarriers;

        @SerializedName("WHITE_LIST_OF_DOMAINS")
        private List<String> mWhiteListedDomains;

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
     * If TYPE is not "webview" in any letter case, defaults to "native"
     */
    public static class EnrollmentConfig {
        @SerializedName("WEBVIEW")
        private WebViewConfig mWebViewConfig;

        @SerializedName("TYPE")
        private String mCourseEnrollmentType;

        public boolean isWebviewCourseDiscoveryEnabled() {
            if (mCourseEnrollmentType == null) {
                return false;
            }

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

        public boolean isWebCourseSearchEnabled() {
            return mWebViewConfig.isWebCourseSearchEnabled();
        }
    }

    public static class WebViewConfig {
        @SerializedName("COURSE_SEARCH_URL")
        private String mSearchUrl;

        @SerializedName("COURSE_INFO_URL_TEMPLATE")
        private String mCourseInfoUrlTemplate;

        @SerializedName("SEARCH_BAR_ENABLED")
        private boolean mSearchBarEnabled;

        public String getCourseSearchUrl() {
            return mSearchUrl;
        }

        public String getCourseInfoUrlTemplate() {
            return mCourseInfoUrlTemplate;
        }

        public boolean isWebCourseSearchEnabled() {
            return mSearchBarEnabled;
        }
    }

    public static class FacebookConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("FACEBOOK_APP_ID")
        private String mFacebookAppId;

        public FacebookConfig(boolean mEnabled, String mFacebookAppId) {
            this.mEnabled = mEnabled;
            this.mFacebookAppId = mFacebookAppId;
        }

        public FacebookConfig() {
        }

        public boolean isEnabled() {
            return mEnabled && !TextUtils.isEmpty(mFacebookAppId);
        }

        public String getFacebookAppId() {
            return mFacebookAppId;
        }
    }

    public static class GoogleConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        public GoogleConfig(boolean mEnabled) {
            this.mEnabled = mEnabled;
        }

        public GoogleConfig() {
        }

        public boolean isEnabled() {
            return mEnabled;
        }
    }

    public static class TwitterConfig {
        @SerializedName("HASHTAG")
        private String mHashTag;

        public String getHashTag() {
            return mHashTag;
        }
    }

    public static class FabricConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("FABRIC_KEY")
        private String mFabricKey;

        @SerializedName("FABRIC_BUILD_SECRET")
        private String mFabricBuildSecret;

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

    public static class NewRelicConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("NEW_RELIC_KEY")
        private String mNewRelicKey;

        public boolean isEnabled() {
            return mEnabled && !TextUtils.isEmpty(mNewRelicKey);
        }

        public String getNewRelicKey() {
            return mNewRelicKey;
        }
    }

    public static class TestAccountConfig {
        @SerializedName("NAME")
        private String mName;

        @SerializedName("PASSWORD")
        private String mPassword;

        public String getName() {
            return mName;
        }

        public String getPassword() {
            return mPassword;
        }
    }

    public static class EndToEndConfig {
        private static final String DEFAULT_EMAIL_TEMPLATE = "test-{unique_id}@example.com";

        @SerializedName("EMAIL_TEMPLATE")
        private String mEmailTemplate;

        @SerializedName("TEST_COURSE_ID")
        private String mTestCourseId;

        public String getEmailTemplate() {
            return TextUtils.isEmpty(mEmailTemplate) ? DEFAULT_EMAIL_TEMPLATE : mEmailTemplate;
        }

        public String getTestCourseId() {
            return mTestCourseId;
        }
    }

    public static class SegmentConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("SEGMENT_IO_WRITE_KEY")
        private String mSegmentWriteKey;

        public boolean isEnabled() {
            return mEnabled && !TextUtils.isEmpty(mSegmentWriteKey);
        }

        public String getSegmentWriteKey() {
            return mSegmentWriteKey;
        }
    }

    public static class DomainWhiteListConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("DOMAINS")
        private List<String> mDomains;

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
        if (element != null) {
            return element.getAsString();
        } else {
            return defaultValue;
        }
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        JsonElement element = getObject(key);
        if (element != null) {
            return element.getAsBoolean();
        } else {
            return defaultValue;
        }
    }

    private int getInteger(String key, int defaultValue) {
        JsonElement element = getObject(key);
        if (element != null) {
            return element.getAsInt();
        } else {
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
     *
     * @return
     */
    public boolean isSpeedTestEnabled() {
        return getBoolean(SPEED_TEST_ENABLED, false);
    }

    public boolean isUserProfilesEnabled() {
        return getBoolean(USER_PROFILES_ENABLED, false);
    }

    public boolean isBadgesEnabled() {
        return getBoolean(BADGES_ENABLED, false);
    }

    public boolean isDiscussionsEnabled() {
        return getBoolean(DISCUSSIONS_ENABLED, false);
    }

    public boolean areCertificateLinksEnabled() {
        return getBoolean(CERTIFICATES_ENABLED, false);
    }

    public boolean isCourseSharingEnabled() {
        return getBoolean(COURSE_SHARING_ENABLED, false);
    }

    @NonNull
    public EnrollmentConfig getCourseDiscoveryConfig() {
        return getObjectOrNewInstance(COURSE_ENROLLMENT, EnrollmentConfig.class);
    }

    @NonNull
    public ZeroRatingConfig getZeroRatingConfig() {
        return getObjectOrNewInstance(ZERO_RATING, ZeroRatingConfig.class);
    }

    @NonNull
    public FacebookConfig getFacebookConfig() {
        return getObjectOrNewInstance(FACEBOOK, FacebookConfig.class);
    }

    @NonNull
    public GoogleConfig getGoogleConfig() {
        return getObjectOrNewInstance(GOOGLE, GoogleConfig.class);
    }

    @NonNull
    public TwitterConfig getTwitterConfig() {
        return getObjectOrNewInstance(TWITTER, TwitterConfig.class);
    }

    @NonNull
    public FabricConfig getFabricConfig() {
        return getObjectOrNewInstance(FABRIC, FabricConfig.class);
    }

    @NonNull
    public NewRelicConfig getNewRelicConfig() {
        return getObjectOrNewInstance(NEW_RELIC, NewRelicConfig.class);
    }

    @NonNull
    public SegmentConfig getSegmentConfig() {
        return getObjectOrNewInstance(SEGMENT_IO, SegmentConfig.class);
    }

    @NonNull
    public TestAccountConfig getTestAccountConfig() {
        return getObjectOrNewInstance(TEST_ACCOUNT, TestAccountConfig.class);
    }

    @NonNull
    public EndToEndConfig getEndToEndConfig() {
        return getObjectOrNewInstance(END_TO_END_TEST, EndToEndConfig.class);
    }

    @NonNull
    private <T> T getObjectOrNewInstance(@NonNull String key, @NonNull Class<T> cls) {
        JsonElement element = getObject(key);
        if (element != null) {
            Gson gson = new Gson();
            return gson.fromJson(element, cls);
        } else {
            try {
                return cls.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
