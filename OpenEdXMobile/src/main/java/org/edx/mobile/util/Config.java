package org.edx.mobile.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.phrase.Phrase;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.core.IEdxEnvironment;
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
    private static final String APP_UPDATE_URIS = "APP_UPDATE_URIS";
    private static final String ORGANIZATION_CODE = "ORGANIZATION_CODE";

    /* Composite configuration keys */
    private static final String DISCOVERY = "DISCOVERY";
    private static final String PROGRAM = "PROGRAM";
    private static final String ZERO_RATING = "ZERO_RATING";
    private static final String FACEBOOK = "FACEBOOK";
    private static final String MICROSOFT = "MICROSOFT";
    private static final String GOOGLE = "GOOGLE";
    private static final String TWITTER = "TWITTER";
    private static final String BRANCH = "BRANCH";
    private static final String NEW_RELIC = "NEW_RELIC";
    private static final String SEGMENT_IO = "SEGMENT_IO";
    private static final String FIREBASE = "FIREBASE";
    private static final String PUSH_NOTIFICATIONS_FLAG = "PUSH_NOTIFICATIONS";
    private static final String WHITE_LIST_OF_DOMAINS = "WHITE_LIST_OF_DOMAINS";
    private static final String API_URL_VERSION = "API_URL_VERSION";
    private static final String YOUTUBE_PLAYER = "YOUTUBE_PLAYER";
    private static final String AGREEMENT_URLS = "AGREEMENT_URLS";

    // Features
    private static final String USER_PROFILES_ENABLED = "USER_PROFILES_ENABLED";
    private static final String DISCUSSIONS_ENABLED = "DISCUSSIONS_ENABLED";
    private static final String CERTIFICATES_ENABLED = "CERTIFICATES_ENABLED";
    private static final String COURSE_SHARING_ENABLED = "COURSE_SHARING_ENABLED";
    private static final String BADGES_ENABLED = "BADGES_ENABLED";
    private static final String END_TO_END_TEST = "END_TO_END_TEST";
    private static final String NEW_LOGISTRATION_ENABLED = "NEW_LOGISTRATION_ENABLED";
    private static final String DISCUSSIONS_ENABLE_PROFILE_PICTURE_PARAM = "DISCUSSIONS_ENABLE_PROFILE_PICTURE_PARAM";
    private static final String REGISTRATION_ENABLED = "REGISTRATION_ENABLED";
    private static final String APP_REVIEWS_ENABLED = "APP_REVIEWS_ENABLED";
    private static final String VIDEO_TRANSCRIPT_ENABLED = "VIDEO_TRANSCRIPT_ENABLED";
    private static final String USING_VIDEO_PIPELINE = "USING_VIDEO_PIPELINE";
    private static final String COURSE_DATES_ENABLED = "COURSE_DATES_ENABLED";
    private static final String WHATS_NEW_ENABLED = "WHATS_NEW_ENABLED";
    private static final String COURSE_VIDEOS_ENABLED = "COURSE_VIDEOS_ENABLED";
    private static final String DOWNLOAD_TO_SD_CARD_ENABLED = "DOWNLOAD_TO_SD_CARD_ENABLED";

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

    public static class DiscoveryConfig {
        @SerializedName("COURSE")
        private CourseDiscoveryConfig courseDiscoveryConfig;

        @SerializedName("PROGRAM")
        private ProgramDiscoveryConfig programDiscoveryConfig;

        @SerializedName("DEGREE")
        private DegreeDiscoveryConfig degreeDiscoveryConfig;

        public enum DiscoveryType {
            WEBVIEW,
            NATIVE
        }

        public CourseDiscoveryConfig getCourseDiscoveryConfig() {
            return courseDiscoveryConfig;
        }

        public ProgramDiscoveryConfig getProgramDiscoveryConfig() {
            return programDiscoveryConfig;
        }

        public DegreeDiscoveryConfig getDegreeDiscoveryConfig() {
            return degreeDiscoveryConfig;
        }
    }

    public static class CourseDiscoveryConfig {
        @SerializedName("TYPE")
        private String typeConfig;

        @SerializedName("WEBVIEW")
        private WebViewConfig webViewConfig;

        @Nullable
        private DiscoveryConfig.DiscoveryType getDiscoveryType() {
            if (null == typeConfig) {
                return null;
            }
            return DiscoveryConfig.DiscoveryType.valueOf(typeConfig.toUpperCase(Locale.US));
        }

        public boolean isDiscoveryEnabled() {
            return getDiscoveryType() != null;
        }

        public boolean isWebviewDiscoveryEnabled() {
            return getDiscoveryType() == DiscoveryConfig.DiscoveryType.WEBVIEW;
        }

        public WebViewConfig getWebViewConfig() {
            return webViewConfig;
        }

        public String getBaseUrl() {
            return null == webViewConfig ? null : webViewConfig.getBaseUrl();
        }

        public String getInfoUrlTemplate() {
            return null == webViewConfig ? null : webViewConfig.getInfoUrlTemplate();
        }

        public boolean isSearchEnabled() {
            return null != webViewConfig && webViewConfig.isSearchEnabled();
        }

        public boolean isSubjectFilterEnabled() {
            return null != webViewConfig && webViewConfig.isSubjectFilterEnabled();
        }
    }

    public static class ProgramDiscoveryConfig {
        @SerializedName("TYPE")
        private String typeConfig;

        @SerializedName("WEBVIEW")
        private WebViewConfig webViewConfig;

        @Nullable
        private DiscoveryConfig.DiscoveryType getDiscoveryType() {
            if (null == typeConfig) {
                return null;
            }
            return DiscoveryConfig.DiscoveryType.valueOf(typeConfig.toUpperCase(Locale.US));
        }

        public boolean isDiscoveryEnabled(@NonNull IEdxEnvironment environment) {
            return getDiscoveryType() != null &&
                    ConfigUtil.Companion.isCourseDiscoveryEnabled(environment) &&
                    ConfigUtil.Companion.isCourseWebviewDiscoveryEnabled(environment)
                    ;
        }

        public boolean isWebviewDiscoveryEnabled() {
            return getDiscoveryType() == DiscoveryConfig.DiscoveryType.WEBVIEW;
        }

        public WebViewConfig getWebViewConfig() {
            return webViewConfig;
        }

        public String getBaseUrl() {
            return null == webViewConfig ? null : webViewConfig.getBaseUrl();
        }

        public String getInfoUrlTemplate() {
            return null == webViewConfig ? null : webViewConfig.getInfoUrlTemplate();
        }

        public boolean isSearchEnabled() {
            return null != webViewConfig && webViewConfig.isSearchEnabled();
        }
    }

    public static class DegreeDiscoveryConfig {
        @SerializedName("TYPE")
        private String typeConfig;

        @SerializedName("WEBVIEW")
        private WebViewConfig webViewConfig;

        @Nullable
        private DiscoveryConfig.DiscoveryType getDiscoveryType() {
            if (null == typeConfig) {
                return null;
            }
            return DiscoveryConfig.DiscoveryType.valueOf(typeConfig.toUpperCase(Locale.US));
        }

        public boolean isDiscoveryEnabled(@NonNull IEdxEnvironment environment) {
            return getDiscoveryType() != null && ConfigUtil.Companion.isProgramDiscoveryEnabled(environment) &&
                    ConfigUtil.Companion.isProgramWebviewDiscoveryEnabled(environment)
                    ;
        }

        public WebViewConfig getWebViewConfig() {
            return webViewConfig;
        }

        public String getBaseUrl() {
            return null == webViewConfig ? null : webViewConfig.getBaseUrl();
        }

        public String getInfoUrlTemplate() {
            return null == webViewConfig ? null : webViewConfig.getInfoUrlTemplate();
        }

        public boolean isSearchEnabled() {
            return null != webViewConfig && webViewConfig.isSearchEnabled();
        }
    }

    public static class WebViewConfig {
        @SerializedName("BASE_URL")
        private String baseUrl;

        @SerializedName("DETAIL_TEMPLATE")
        private String detailTemplate;

        @SerializedName("SEARCH_ENABLED")
        private boolean searchEnabled;

        @SerializedName("SUBJECT_FILTER_ENABLED")
        private boolean subjectFilterEnabled;

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getInfoUrlTemplate() {
            return detailTemplate;
        }

        public boolean isSearchEnabled() {
            return searchEnabled;
        }

        public boolean isSubjectFilterEnabled() {
            return subjectFilterEnabled;
        }
    }

    public static class ProgramConfig {
        @SerializedName("ENABLED")
        private boolean enabled;

        @SerializedName("PROGRAM_URL")
        private String url;

        @SerializedName("PROGRAM_DETAIL_URL_TEMPLATE")
        private String detailUrlTemplate;

        public boolean isEnabled() {
            // TODO Disable program feature for kitkat users, See Jira story LEARNER-6625 for more details.
            return enabled && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT;
        }

        public String getUrl() {
            return url;
        }

        public String getDetailUrlTemplate() {
            return detailUrlTemplate;
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

    public static class MicrosoftConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        public MicrosoftConfig(boolean mEnabled) {
            this.mEnabled = mEnabled;
        }

        public MicrosoftConfig() {
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

    public static class BranchConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("KEY")
        private String key;

        @SerializedName("SECRET")
        private String secret;

        public boolean isEnabled() {
            return mEnabled;
        }

        public String getKey() {
            return key;
        }

        public String getSecret() {
            return secret;
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

    public static class FirebaseConfig {
        @SerializedName("ENABLED")
        private boolean mEnabled;

        @SerializedName("ANALYTICS_SOURCE")
        private AnalyticsSource mAnalyticsSource;

        @SerializedName("CLOUD_MESSAGING_ENABLED")
        private boolean mCloudMessagingEnabled;

        public enum AnalyticsSource {
            @SerializedName("segment")
            SEGMENT,
            @SerializedName("firebase")
            FIREBASE,
            @SerializedName("none")
            NONE
        }

        public boolean isEnabled() {
            return mEnabled;
        }

        public boolean isAnalyticsSourceSegment() {
            return mEnabled && mAnalyticsSource == AnalyticsSource.SEGMENT;
        }

        public boolean isAnalyticsSourceFirebase() {
            return mEnabled && mAnalyticsSource == AnalyticsSource.FIREBASE;
        }

        public boolean areNotificationsEnabled() {
            return mEnabled && mCloudMessagingEnabled;
        }
    }

    public static class ApiUrlVersionConfig {
        private static final String DEFAULT_API_VERSION = "v1";
        @SerializedName("BLOCKS")
        private String blocksApiVersion;
        @SerializedName("REGISTRATION")
        private String registrationApiVersion;

        public ApiUrlVersionConfig() {
            blocksApiVersion = DEFAULT_API_VERSION;
        }

        public ApiUrlVersionConfig(@NonNull String blocksApiVersion) {
            this.blocksApiVersion = blocksApiVersion;
        }

        public String getBlocksApiVersion() {
            return blocksApiVersion != null ? blocksApiVersion : DEFAULT_API_VERSION;
        }

        public String getRegistrationApiVersion() {
            return registrationApiVersion != null ? registrationApiVersion : DEFAULT_API_VERSION;
        }
    }

    public static class AgreementUrlsConfig {
        @SerializedName("EULA_URL")
        private String eulaUrl;

        @SerializedName("TOS_URL")
        private String tosUrl;

        @SerializedName("PRIVACY_POLICY_URL")
        private String privacyPolicyUrl;

        public String getEulaUrl() {
            return eulaUrl;
        }

        public String getTosUrl() {
            return tosUrl;
        }

        public String getPrivacyPolicyUrl() {
            return privacyPolicyUrl;
        }

        public boolean isAtleastOneAgreementUrlAvailable() {
            return !TextUtils.isEmpty(eulaUrl) || !TextUtils.isEmpty(tosUrl) || !TextUtils.isEmpty(privacyPolicyUrl);
        }
    }

    public static class YoutubePlayerConfig {
        @SerializedName("ENABLED")
        private boolean enabled;

        @SerializedName("API_KEY")
        private String apiKey;

        public boolean isYoutubePlayerEnabled() {
            return enabled;
        }

        public String getApiKey() {
            return apiKey;
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

    /**
     * @return A list of URIs of app stores, or an empty list if none are available.
     */
    @NonNull
    public List<Uri> getAppStoreUris() {
        //noinspection unchecked
        final List<String> uriStrings = getObjectOrNewInstance(APP_UPDATE_URIS, ArrayList.class);
        final List<Uri> uris = new ArrayList<>(uriStrings.size());
        for (final String uriString : uriStrings) {
            if (uriString != null) {
                // Replace the 'application_id' token with the actual application ID.
                uris.add(Uri.parse(Phrase.from(uriString)
                        .put("application_id", BuildConfig.APPLICATION_ID)
                        .format()
                        .toString()));
            }
        }
        return uris;
    }

    public String getOrganizationCode() {
        return getString(ORGANIZATION_CODE);
    }

    public boolean areFirebasePushNotificationsEnabled() {
        return getFirebaseConfig().areNotificationsEnabled() && arePushNotificationEnabled();
    }

    private boolean arePushNotificationEnabled() {
        return getBoolean(PUSH_NOTIFICATIONS_FLAG, false);
    }

    public boolean isNewLogistrationEnabled() {
        return getBoolean(NEW_LOGISTRATION_ENABLED, false);
    }

    public boolean isDiscussionProfilePicturesEnabled() {
        return getBoolean(DISCUSSIONS_ENABLE_PROFILE_PICTURE_PARAM, false);
    }

    public boolean isRegistrationEnabled() {
        return getBoolean(REGISTRATION_ENABLED, true);
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

    public boolean isAppReviewsEnabled() {
        return getBoolean(APP_REVIEWS_ENABLED, false);
    }

    public boolean areCertificateLinksEnabled() {
        return getBoolean(CERTIFICATES_ENABLED, false);
    }

    public boolean isCourseSharingEnabled() {
        return getBoolean(COURSE_SHARING_ENABLED, false);
    }

    public boolean isVideoTranscriptEnabled() {
        return getBoolean(VIDEO_TRANSCRIPT_ENABLED, false);
    }

    public boolean isUsingVideoPipeline() {
        return getBoolean(USING_VIDEO_PIPELINE, true);
    }

    public boolean isCourseDatesEnabled() {
        return getBoolean(COURSE_DATES_ENABLED, false);
    }

    public boolean isWhatsNewEnabled() {
        return getBoolean(WHATS_NEW_ENABLED, false);
    }

    public boolean isCourseVideosEnabled() {
        return getBoolean(COURSE_VIDEOS_ENABLED, true);
    }

    public boolean isDownloadToSDCardEnabled() {
        return getBoolean(DOWNLOAD_TO_SD_CARD_ENABLED, false);
    }

    @NonNull
    public ProgramConfig getProgramConfig() {
        return getObjectOrNewInstance(PROGRAM, ProgramConfig.class);
    }

    @Nullable
    public DiscoveryConfig getDiscoveryConfig() {
        return getObjectOrNewInstance(DISCOVERY, DiscoveryConfig.class);
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
    public MicrosoftConfig getMicrosoftConfig() {
        return getObjectOrNewInstance(MICROSOFT, MicrosoftConfig.class);
    }

    @NonNull
    public TwitterConfig getTwitterConfig() {
        return getObjectOrNewInstance(TWITTER, TwitterConfig.class);
    }

    @NonNull
    public BranchConfig getBranchConfig() {
        return getObjectOrNewInstance(BRANCH, BranchConfig.class);
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
    public FirebaseConfig getFirebaseConfig() {
        return getObjectOrNewInstance(FIREBASE, FirebaseConfig.class);
    }

    @NonNull
    public EndToEndConfig getEndToEndConfig() {
        return getObjectOrNewInstance(END_TO_END_TEST, EndToEndConfig.class);
    }

    @NonNull
    public ApiUrlVersionConfig getApiUrlVersionConfig() {
        return getObjectOrNewInstance(API_URL_VERSION, ApiUrlVersionConfig.class);
    }

    @NonNull
    public YoutubePlayerConfig getYoutubePlayerConfig() {
        return getObjectOrNewInstance(YOUTUBE_PLAYER, YoutubePlayerConfig.class);
    }

    @NonNull
    public AgreementUrlsConfig getAgreementUrlsConfig() {
        return getObjectOrNewInstance(AGREEMENT_URLS, AgreementUrlsConfig.class);
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
