package org.edx.mobile.module.prefs;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.Sha1Util;

/**
 * This is a Utility for reading and writing to shared preferences.
 * This class also contains the constants for the preference names and the keys. 
 * These constants are defined in inner classes <code>Pref</code> and <code>Key</code>. 
 *
 */
public class PrefManager {
    
    private Context context;
    private String prefName;
    private static final Logger logger = new Logger(PrefManager.class.getName());

    //FIXME - we should use MAApplication's context to clean up
    //the code.
    public PrefManager(Context context, String prefName) {
        if ( MainApplication.instance() != null )
            this.context = MainApplication.instance().getApplicationContext();
        else
            this.context = context;
        this.prefName = prefName;
    }
    
    /**
     * Puts given key-value pair to the Shared Preferences.
     * @param key
     * @param value - String
     */
    public void put(String key, String value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putString(key, value).commit();
    }
    
    /**
     * Puts given key-value pair to the Shared Preferences.
     * @param key
     * @param value - boolean
     */
    public void put(String key, boolean value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putBoolean(key, value).commit();
    }
    
    /**
     * Puts given key-value pair to the Shared Preferences.
     * @param key
     * @param value - long
     */
    public void put(String key, long value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putLong(key, value).commit();
    }
    
    /**
     * Puts given key-value pair to the Shared Preferences.
     * @param key
     * @param value - float
     */
    public void put(String key, float value) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putFloat(key, value).commit();
    }
    
    /**
     * Returns String value for the given key, null if no value is found.
     * @param key
     * @return String 
     */
    public String getString(String key) {
        if(context!=null){
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getString(key, null);
        }
        return null;
    }
    

    /**
     * Returns boolean value for the given key, can set default value as well.
     * @param key,default value
     * @return boolean
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        if(context!=null) {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getBoolean(key, defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * Returns long value for the given key, -1 if no value is found.
     * @param key
     * @return long
     */
    public long getLong(String key) {
        if(context!=null){
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getLong(key, -1);
        }
        return -1;
    }
    
    /**
     * Returns float value for the given key, -1 if no value is found.
     * @param key
     * @return float
     */
    public float getFloat(String key) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getFloat(key, -1);
    }

    /**
     * Returns float value for the given key, defaultValue if no value is found.
     * @param key
     * @param defaultValue
     * @return float
     */
    public float getFloat(String key, float defaultValue) {
        if(context!=null){
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getFloat(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns current user's profile from the preferences.
     * @return
     */
    @Nullable
    public ProfileModel getCurrentUserProfile() {
        String json = getString(PrefManager.Key.PROFILE_JSON);
        if (json == null) {
            return null;
        }
        
        Gson gson = new GsonBuilder().create();
        ProfileModel res = gson.fromJson(json, ProfileModel.class);
        res.json = json;
        
        return res;
    }
    
    /**
     * Returns current user's profile from the preferences.
     * @return
     */
    public AuthResponse getCurrentAuth() {
        String json = getString(PrefManager.Key.AUTH_JSON);
        if (json == null) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        AuthResponse res = gson.fromJson(json, AuthResponse.class);
        
        return res;
    }

    /**
     * Clears auth token info and current profile information from preferences.
     */
    public void clearAuth() {
        put(PrefManager.Key.PROFILE_JSON, null);
        put(PrefManager.Key.AUTH_JSON, null);
        put(PrefManager.Key.AUTH_TOKEN_SOCIAL, null);
        put(PrefManager.Key.AUTH_TOKEN_BACKEND, null);
        put(PrefManager.Key.AUTH_TOKEN_SOCIAL_COOKIE, null);
        //assessment webview related session_id
        EdxCookieManager.getSharedInstance().clearWebWiewCookie(MainApplication.instance());
    }

    /**
     *  check if app is currently logged in through Google/Facebook
     */
    public boolean hasAuthTokenSocialCookie(){
        return  null !=  getString(Key.AUTH_TOKEN_SOCIAL_COOKIE);
    }
    
    /**
     * Stores information of last accesses subsection for given id.
     * Modification date is also stored for current time.
     * Synced is marked as FALSE.
     * @param subsectionId
     * @param lastAccessedFlag
     */
    public void putLastAccessedSubsection(String subsectionId, boolean lastAccessedFlag) {
        Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        edit.putString(PrefManager.Key.LASTACCESSED_MODULE_ID, subsectionId);
        edit.putString(PrefManager.Key.LAST_ACCESS_MODIFICATION_TIME, DateUtil.getModificationDate());
        edit.putBoolean(PrefManager.Key.LASTACCESSED_SYNCED_FLAG, lastAccessedFlag);
        edit.commit();
    }
    
    /**
     * Returns true if given courseId's last access is synced with server, false otherwise.
     * @return
     */
    public boolean isSyncedLastAccessedSubsection() {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getBoolean(PrefManager.Key.LASTACCESSED_SYNCED_FLAG, true);
    }


    /**
     * Returns last accessed subsection id for the given course. 
     * @return
     */
    public String getLastAccessedSubsectionId() {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getString(PrefManager.Key.LASTACCESSED_MODULE_ID, null);
    }
    
    /**
     * Returns preference file name that can be used to store information about last accessed subsection.
     * This preference file name is SHA1 hash of a combination of username, courseId and a constant suffix.
     * 
     * @param username
     * @param courseId
     * @return
     * @throws Exception
     */
    public static String getPrefNameForLastAccessedBy(String username, String courseId) {
        String raw = username + "-" + courseId + "-last-accessed-subsection_info";
        try {
            String hash = Sha1Util.SHA1(raw);
            return hash;
        } catch(Exception ex) {
            logger.error(ex);
        }
        return raw;
    }

    public static class AppInfoPrefManager extends PrefManager{
        public AppInfoPrefManager(Context context){
            super(context, PrefManager.Pref.APP_INFO);
        }
        public long getAppVersionCode(){
            return getLong(Key.APP_VERSION_CODE);
        }
        public void setAppVersionCode(long code){
            super.put(Key.APP_VERSION_CODE, code);
        }
        public String getAppVersionName(){
            return getString(Key.APP_VERSION_NAME);
        }
        public void setAppVersionName(String code){
            super.put(Key.APP_VERSION_NAME, code);
        }
        public boolean isNotificationEnabled(){
            return getBoolean(Key.NOTIFICATION, false);
        }
        public void setNotificationEnabled(boolean enabled){
            super.put(Key.NOTIFICATION, enabled);
        }
        public boolean isAppUpgradeNeedSyncWithParse(){
            return getBoolean(Key.AppUpgradeNeedSyncWithParse, false);
        }
        public void setAppUpgradeNeedSyncWithParse(boolean enabled){
            super.put(Key.AppUpgradeNeedSyncWithParse, enabled);
        }
        public boolean isAppSettingNeedSyncWithParse(){
            return getBoolean(Key.AppSettingNeedSyncWithParse, false);
        }
        public void setAppSettingNeedSyncWithParse(boolean enabled){
            super.put(Key.AppSettingNeedSyncWithParse, enabled);
        }
        public String getPrevNotificationHashKey(){
            return getString(Key.AppNotificationPushHash);
        }
        public void setPrevNotificationHashKey(String code){
            super.put(Key.AppNotificationPushHash, code);
        }
    }

    public static class UserPrefManager extends PrefManager {

        public UserPrefManager(Context context) {
            super(context, Pref.USER_PREF);
        }

        public boolean isUserPrefVideoModel(){
            //default is full mode
            return getBoolean(Key.UserPrefVideoModel, false);
        }
        public void setUserPrefVideoModel(boolean enabled){
            super.put(Key.UserPrefVideoModel, enabled);
        }
        public long getLastCourseStructureFetch(String courseId){
            return getLong(Key.LAST_COURSE_STRUCTURE_FETCH + "_" + courseId);
        }
        public void setLastCourseStructureFetch(String courseId, long timestamp){
            super.put(Key.LAST_COURSE_STRUCTURE_FETCH + "_" + courseId, timestamp);
        }
    }
    /**
     * Contains preference name constants.
     *
     */
    public static final class Pref {
        public static final String LOGIN = "pref_login";
        public static final String WIFI = "pref_wifi";
        public static final String VIDEOS = "pref_videos";
        public static final String FEATURES = "features";
        public static final String APP_INFO = "pref_app_info";
        public static final String USER_PREF = "pref_user";

    }
    
    /**
     * Contains preference key constants.
     *
     */
    public static final class Key {
        public static final String PROFILE_JSON = "profile_json";
        public static final String AUTH_JSON = "auth_json";
        //TODO- need to rename these constants. causing confusion
        public static final String AUTH_TOKEN_SOCIAL = "facebook_token";
        public static final String AUTH_TOKEN_BACKEND = "google_token";
        public static final String AUTH_TOKEN_SOCIAL_COOKIE = "social_auth_cookie";
        public static final String DOWNLOAD_ONLY_ON_WIFI = "download_only_on_wifi";
        public static final String DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG = "download_off_wifi_dialog_flag";
        public static final String TRANSCRIPT_LANGUAGE = "transcript_language";
        public static final String ALLOW_SOCIAL_FEATURES = "allow_social_features";
        public static final String LAST_ACCESS_MODIFICATION_TIME = "last_access_modification_time";
        public static final String LASTACCESSED_MODULE_ID = "last_access_module_id";
        public static final String LASTACCESSED_SYNCED_FLAG = "lastaccess_synced_flag";
        public static final String SEGMENT_KEY_BACKEND = "segment_backend";
        public static final String SHARE_COURSES = "share_courses";
        public static final String SPEED_TEST_KBPS = "speed_test_kbps";
        public static final String APP_VERSION_NAME = "app_version_name";
        public static final String APP_VERSION_CODE = "app_version_code";
        public static final String NOTIFICATION_PROFILE_JSON = "notification_profile_json";
        private static final String NOTIFICATION = "notification";
        public static final String AppNotificationPushHash = "AppNotificationPushHash";
        public static final String AppUpgradeNeedSyncWithParse = "AppUpgradeNeedSyncWithParse";
        public static final String AppSettingNeedSyncWithParse = "AppSettingNeedSyncWithParse";
        public static final String UserPrefVideoModel = "UserPrefVideoModel";
        public static final String LAST_COURSE_STRUCTURE_FETCH = "LastCourseStructureFetch";


    }
    
    public static final class Value {
        /*
         * These values are used in API endpoint
         */
        public static final String BACKEND_FACEBOOK             = "facebook";
        public static final String BACKEND_GOOGLE                 = "google-oauth2";
    }
}
