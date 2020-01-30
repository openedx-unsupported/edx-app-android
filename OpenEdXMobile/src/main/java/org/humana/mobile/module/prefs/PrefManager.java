package org.humana.mobile.module.prefs;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.humana.mobile.base.MainApplication;

import java.util.List;

/**
 * This is a Utility for reading and writing to shared preferences.
 * This class also contains the constants for the preference names and the keys.
 * These constants are defined in inner classes <code>Pref</code> and <code>Key</code>.
 */
public class PrefManager {

    private Context context;
    private String prefName;

    //FIXME - we should use MAApplication's context to clean up
    //the code.
    public PrefManager(Context context, String prefName) {
        if (MainApplication.instance() != null)
            this.context = MainApplication.instance().getApplicationContext();
        else
            this.context = context;
        this.prefName = prefName;
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - String
     */
    public void put(String key, String value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putString(key, value).commit();
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - boolean
     */
    public void put(String key, boolean value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putBoolean(key, value).commit();
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - long
     */
    public void put(String key, long value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putLong(key, value).commit();
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - float
     */
    public void put(String key, float value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putFloat(key, value).commit();
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - int
     */
    public void put(String key, int value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putInt(key, value).commit();
    }

    /**
     * Returns String value for the given key, null if no value is found.
     *
     * @param key
     * @return String
     */
    public String getString(String key) {
        if (context != null) {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getString(key, null);
        }
        return null;
    }


    /**
     * Returns boolean value for the given key, can set default value as well.
     *
     * @param key,default value
     * @return boolean
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if (context != null) {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getBoolean(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns long value for the given key, -1 if no value is found.
     *
     * @param key
     * @return long
     */
    public long getLong(String key) {
        if (context != null) {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getLong(key, -1);
        }
        return -1;
    }

    /**
     * Returns float value for the given key, -1.0 if no value is found.
     *
     * @param key
     * @return float
     */
    public float getFloat(String key) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getFloat(key, -1.0f);
    }

    /**
     * Returns float value for the given key, defaultValue if no value is found.
     *
     * @param key
     * @param defaultValue
     * @return float
     */
    public float getFloat(String key, float defaultValue) {
        if (context != null) {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getFloat(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns int value for the given key, -1 if no value is found.
     *
     * @param key
     * @return int
     */
    public int getInt(String key) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getInt(key, -1);
    }

    public static class AppInfoPrefManager extends PrefManager {
        public AppInfoPrefManager(Context context) {
            super(context, PrefManager.Pref.APP_INFO);
        }

        public long getAppVersionCode() {
            return getLong(Key.APP_VERSION_CODE);
        }

        public void setAppVersionCode(long code) {
            super.put(Key.APP_VERSION_CODE, code);
        }

        public String getAppVersionName() {
            return getString(Key.APP_VERSION_NAME);
        }

        public void setAppVersionName(String code) {
            super.put(Key.APP_VERSION_NAME, code);
        }

        public boolean isNotificationEnabled() {
            return getBoolean(Key.NOTIFICATION, false);
        }

        public void setNotificationEnabled(boolean enabled) {
            super.put(Key.NOTIFICATION, enabled);
        }

        public boolean isAppUpgradeNeedSyncWithParse() {
            return getBoolean(Key.AppUpgradeNeedSyncWithParse, false);
        }

        public void setAppUpgradeNeedSyncWithParse(boolean enabled) {
            super.put(Key.AppUpgradeNeedSyncWithParse, enabled);
        }

        public boolean isAppSettingNeedSyncWithParse() {
            return getBoolean(Key.AppSettingNeedSyncWithParse, false);
        }

        public void setAppSettingNeedSyncWithParse(boolean enabled) {
            super.put(Key.AppSettingNeedSyncWithParse, enabled);
        }

        public String getPrevNotificationHashKey() {
            return getString(Key.AppNotificationPushHash);
        }

        public void setPrevNotificationHashKey(String code) {
            super.put(Key.AppNotificationPushHash, code);
        }

        public float getAppRating() {
            return getFloat(Key.APP_RATING);
        }

        public void setAppRating(float appRating) {
            super.put(Key.APP_RATING, appRating);
        }

        public String getLastRatedVersion() {
            return getString(Key.LAST_RATED_VERSION);
        }

        public void setLastRatedVersion(String versionName) {
            super.put(Key.LAST_RATED_VERSION, versionName);
        }

        @Nullable
        public String getWhatsNewShownVersion() {
            return getString(Key.WHATS_NEW_SHOWN_FOR_VERSION);
        }

        public void setWhatsNewShown(@NonNull String version) {
            super.put(Key.WHATS_NEW_SHOWN_FOR_VERSION, version);
        }
    }

    /**
     * Contains preference name constants. These must be unique.
     */
    public static final class Pref {
        public static final String LOGIN = "pref_login";
        public static final String WIFI = "pref_wifi";
        public static final String VIDEOS = "pref_videos";
        public static final String FEATURES = "features";
        public static final String APP_INFO = "pref_app_info";
        public static final String USER_PREF = "pref_user";

        public static String[] getAll() {
            return new String[]{LOGIN, WIFI, VIDEOS, FEATURES, APP_INFO, USER_PREF};
        }

        public static String[] getAllPreferenceFileNames() {
            String[] preferencesFilesList = PrefManager.Pref.getAll();
            for (int i = 0; i < preferencesFilesList.length; i++) {
                preferencesFilesList[i] += ".xml";
            }
            return preferencesFilesList;
        }
    }

    /**
     * Contains preference key constants.
     */
    public static final class Key {
        public static final String PROFILE_JSON = "profile_json";
        public static final String AUTH_JSON = "auth_json";
        public static final String AUTH_EMAIL = "email";
        public static final String PROFILE_IMAGE = "profile_image";
        //TODO- need to rename these constants. causing confusion
        public static final String AUTH_TOKEN_SOCIAL = "facebook_token";
        public static final String AUTH_TOKEN_BACKEND = "google_token";
        public static final String AUTH_TOKEN_SOCIAL_COOKIE = "social_auth_cookie";
        public static final String DOWNLOAD_ONLY_ON_WIFI = "download_only_on_wifi";
        public static final String DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG = "download_off_wifi_dialog_flag";
        public static final String TRANSCRIPT_LANGUAGE = "transcript_language";
        public static final String ANALYTICS_KEY_BACKEND = "segment_backend";
        public static final String SPEED_TEST_KBPS = "speed_test_kbps";
        public static final String APP_VERSION_NAME = "app_version_name";
        public static final String APP_VERSION_CODE = "app_version_code";
        public static final String NOTIFICATION_PROFILE_JSON = "notification_profile_json";
        private static final String NOTIFICATION = "notification";
        public static final String AppNotificationPushHash = "AppNotificationPushHash";
        public static final String AppUpgradeNeedSyncWithParse = "AppUpgradeNeedSyncWithParse";
        public static final String AppSettingNeedSyncWithParse = "AppSettingNeedSyncWithParse";

        // Preference to save user app rating
        public static final String APP_RATING = "APP_RATING";
        // Preference to save app version when user rated last time
        public static final String LAST_RATED_VERSION = "LAST_RATED_VERSION";
        // Preference to keep track if Whats New feature has been shown for a specific version
        public static final String WHATS_NEW_SHOWN_FOR_VERSION = "WHATS_NEW_SHOWN_FOR_VERSION";
        // Preference to keep track of Bulk download switch for a Course ID
        public static final String BULK_DOWNLOAD_FOR_COURSE_ID = "BULK_DOWNLOAD_%s";

        //TTA

        public static final String FIRST_LAUNCH = "first_launch";
        public static final String FIRST_LOGIN = "first_login";
        public static final String CURRENT_BREADCRUMB = "current_breadcrumb";

        //mx:Arjun cookies and its entry time
        public static final String USER_CONNECT_LOGIN_COOKIE = "UserConnectLoginCookie";
        public static final String USER_CONNECT_LOGIN_COOKIE_TIME_STAMP = "UserConnectLoginCookieStamp";
        public static final String USER_COURSE_ANSWER_DATA = "UserCourseAnswerData";
        public static final String USER_CURRENT_FRAG_NAME_DATA = "UserCurrentFragNameData";
        public static final String USER_OLD_DATA_MIGRATION_STATUS="UserOldDataMigrationStatus";
        public static final String USER_APP_MIN_MIGRATION_STATUS="UserAppMiniMigrationStatus";
        public static final String USER_CURRENTDOWNLOADING_SCROM_INFO="UserCurrentDownloadingScromInfo";
        public static final String USER_GENERIC_FIELD_ATTRIBUTE_INFO="UserGenericFieldAttributeInfo";


        public static final String USER_PROFIILE_UPDATE_PAGE_NAME_INFO="UserProfileUpdatePageNameInfo";
        public static final String USER_PROFIILE_UPDATE_PAGE_GENDER_INFO="UserProfileUpdatePageGenderInfo";
        public static final String USER_PROFIILE_UPDATE_PAGE_PMIS_CODE_INFO="UserProfileUpdatePagePMISCodeInfo";
        public static final String USER_PROFIILE_UPDATE_PAGE_CLASSES_TAUGHT_INFO="UserProfileUpdatePageClassesTaughtInfo";

        public static final String USER_PROFIILE_UPDATE_PAGE_STATE_INFO="UserProfileUpdatePageStateInfo";
        public static final String USER_PROFIILE_UPDATE_PAGE_DISTRICT_INFO="UserProfileUpdatePageDistrictInfo";
        public static final String USER_PROFIILE_UPDATE_PAGE_DIET_INFO="UserProfileUpdatePageDietInfo";
        public static final String USER_PROFIILE_UPDATE_PAGE_BLOCK_INFO="UserProfileUpdatePageBlockInfo";
        public static final String USER_PROFIILE_UPDATE_PAGE_TITLE_INFO="UserProfileUpdatePageTitleInfo";

        public static final String IS_USER_COURSE_DASHBOARD_LOADED="Is_User_Course_Dashboard_Loaded";


        public static final String WP_AUTH_JSON = "wp_auth_json";
        public static final String WP_PROFILE_JSON = "wp_profile_json";

        //Mx Chirag: To store search filters
        public static final String PROGRAM_FILTERS_JSON = "program_filters_json";

        public static final String PROGRAM_ID = "program_id";
        public static final String PROGRAM_TITLE = "program_title";
        public static final String ROLE = "role";
        public static final String parentId = "parent_id";
        public static final String SECTION_ID = "section_id";

        //MX Ankit: To store app update
        public static final String IS_UPDATE_SEEN_DATE = "update_seen";
        public static final String LATEST_APP_INFO_JSON = "latest_app_info_json";

        public static final String LOGINUSERCOOKIE = "login_user_cookie";
        public static final String TYPEFILTER = "type_filter";
        public static final String LangFILTER = "Lang_filter";
        public static final String SESSION_FILTER = "session_filter";
        public static final String Lang_FILTER = "Lang_filter";
        public static final String SESSION_FILTER_TAG = "session_filter_tag";
        public static final String PROGRAM_FILTER = "program_filter";
        public static final String Lang_FILTER_TAG = "Lang_filter_tag";
        public static final String TAG_LIST = "tag_list";
        public static final String PERIOD_FILTER = "period_filter";
        public static final String CURRENT_PERIOD_ID = "current_period_id";
        public static final String CURRENT_PERIOD_TITLE = "current_period_title";
        public static final String CURRENT_PERIOD_LANG = "current_period_lang";
        public static final String CALENDER_START_DATE = "calender_start_date";
        public static final String CALENDER_END_DATE = "calender_start_date";
        public static final String IS_SCHEDULE_TOOLTIP = "is_schedule_tooltip";
        public static final String IS_UNIT_TOOLTIP = "is_unit_tooltip";
        public static final String IS_PROFILE_TOOLTIP = "is_profile_tooltip";
    }

    public static final class Value {
        /*
         * These values are used in API endpoint
         */
        public static final String BACKEND_FACEBOOK = "facebook";
        public static final String BACKEND_GOOGLE = "google-oauth2";
    }

    /**
     * Clears all the shared preferences that are used in the app.
     *
     * @param exceptions Names of the preferences that need to be skipped while clearing.
     */
    public static void nukeSharedPreferences(@NonNull List<String> exceptions) {
        for (String prefName : Pref.getAll()) {
            if (exceptions.contains(prefName)) {
                continue;
            }
            MainApplication.application.getSharedPreferences(
                    prefName, Context.MODE_PRIVATE).edit().clear().apply();
        }
    }
}
