package org.humana.mobile.module.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.humana.mobile.authentication.AuthResponse;
import org.humana.mobile.base.MainApplication;
import org.humana.mobile.model.api.ProfileModel;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.services.EdxCookieManager;
import org.humana.mobile.tta.data.model.UpdateResponse;
import org.humana.mobile.tta.data.model.authentication.FieldInfo;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramFilterTag;
import org.humana.mobile.tta.data.model.program.SelectedFilter;
import org.humana.mobile.tta.wordpress_client.model.WPProfileModel;
import org.humana.mobile.tta.wordpress_client.model.WpAuthResponse;
import org.humana.mobile.user.ProfileImage;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPrefs {

    public List<SelectedFilter> getCachedFilter() {

        final String json = pref.getString(PrefManager.Key.TAG_LIST);
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<List<SelectedFilter>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    public void setCachedFilter(List<SelectedFilter> selectedFilters) {
        pref.put(PrefManager.Key.TAG_LIST, gson.toJson(selectedFilters));
        //sselectedFilters
    }

    public void clearCachedFilter() {
        pref.put(PrefManager.Key.TAG_LIST, gson.toJson(null));
    }

    public enum AuthBackend {
        PASSWORD,
        FACEBOOK,
        GOOGLE
    }

    @NonNull
    private final Gson gson = new GsonBuilder().create();

    @NonNull
    private final PrefManager pref;

    private final SharedPreferences.Editor editor = new SharedPreferences.Editor() {
        @Override
        public SharedPreferences.Editor putString(String key, @androidx.annotation.Nullable String value) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, @androidx.annotation.Nullable Set<String> values) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            return null;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            return null;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            return null;
        }

        @Override
        public SharedPreferences.Editor clear() {
            return null;
        }

        @Override
        public boolean commit() {
            return false;
        }

        @Override
        public void apply() {

        }
    };

    @Inject
    public LoginPrefs(@NonNull Context context) {
        pref = new PrefManager(context, PrefManager.Pref.LOGIN);
    }

    public void storeAuthTokenResponse(@NonNull AuthResponse response, @NonNull AuthBackend backend) {
        pref.put(PrefManager.Key.AUTH_JSON, gson.toJson(response));
        pref.put(PrefManager.Key.ANALYTICS_KEY_BACKEND, analyticsTokenFromAuthBackend(backend));
    }

    public void clearAuthTokenResponse() {
        pref.put(PrefManager.Key.AUTH_JSON, null);
        pref.put(PrefManager.Key.ANALYTICS_KEY_BACKEND, null);
    }

    public void storeRefreshTokenResponse(@NonNull AuthResponse refreshTokenResponse) {
        pref.put(PrefManager.Key.AUTH_JSON, gson.toJson(refreshTokenResponse));
    }

    public void storeUserProfile(@NonNull ProfileModel res) {
        pref.put(PrefManager.Key.PROFILE_JSON, gson.toJson(res));
        clearSocialLoginToken();
    }

    public void clear() {
        clearSocialLoginToken();
        setSubtitleLanguage(null);
        pref.put(PrefManager.Key.PROFILE_JSON, null);
        pref.put(PrefManager.Key.AUTH_JSON, null);
        EdxCookieManager.getSharedInstance(MainApplication.instance()).clearWebWiewCookie();
    }

    public void saveSocialLoginToken(@NonNull String accessToken, @NonNull String backend) {
        pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL, accessToken);
        pref.put(PrefManager.Key.AUTH_TOKEN_BACKEND, backend);
    }

    public void clearSocialLoginToken() {
        pref.put(PrefManager.Key.AUTH_TOKEN_BACKEND, null);
        pref.put(PrefManager.Key.AUTH_TOKEN_SOCIAL, null);
    }

    /**
     * @return language code if subtitles are enabled, or null if subtitles are disabled
     */
    @Nullable
    public String getSubtitleLanguage() {
        final String lang = pref.getString(PrefManager.Key.TRANSCRIPT_LANGUAGE);
        if (android.text.TextUtils.isEmpty(lang)) {
            return null;
        }
        return lang;
    }

    public void setSubtitleLanguage(@Nullable String language) {
        pref.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, language);
    }

    @Nullable
    public String getAuthorizationHeader() {
        final AuthResponse auth = getCurrentAuth();
        if (auth == null || !auth.isSuccess()) {
            // this might be a login with Facebook or Google
            return getSocialLoginAccessToken();
        } else {
            return String.format("%s %s", auth.token_type, auth.access_token);
        }
    }

    @Nullable
    public String getSocialLoginAccessToken() {
        return pref.getString(PrefManager.Key.AUTH_TOKEN_SOCIAL);
    }

    @Nullable
    public String getSocialLoginProvider() {
        return pref.getString(PrefManager.Key.AUTH_TOKEN_BACKEND);
    }

    @Nullable
    public AuthResponse getCurrentAuth() {
        final String json = pref.getString(PrefManager.Key.AUTH_JSON);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, AuthResponse.class);
    }

    @Nullable
    public ProfileModel getCurrentUserProfile() {
        final String json = pref.getString(PrefManager.Key.PROFILE_JSON);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, ProfileModel.class);
    }

    @Nullable
    public String getUsername() {
        final ProfileModel profileModel = getCurrentUserProfile();
        return null == profileModel ? null : profileModel.username;
    }

    @Nullable
    public String getDisplayName() {
        final ProfileModel profileModel = getCurrentUserProfile();
        return null == profileModel ? null : profileModel.name;
    }

    @Nullable
    public String getAuthBackendKeyForSegment() {
        return pref.getString(PrefManager.Key.ANALYTICS_KEY_BACKEND);
    }

    @Nullable
    public String getLastAuthenticatedEmail() {
        return pref.getString(PrefManager.Key.AUTH_EMAIL);
    }

    public void setLastAuthenticatedEmail(@Nullable String emailAddress) {
        pref.put(PrefManager.Key.AUTH_EMAIL, emailAddress);
    }

    public void setProfileImage(@NonNull String username, @Nullable ProfileImage profileImage) {
        if (username.equals(getUsername())) {
            pref.put(PrefManager.Key.PROFILE_IMAGE, gson.toJson(profileImage));
        }
    }

    @Nullable
    public ProfileImage getProfileImage() {
        final String json = pref.getString(PrefManager.Key.PROFILE_IMAGE);
        if (null == json) {
            return null;
        }
        return gson.fromJson(json, ProfileImage.class);
    }

    @NonNull
    private static String analyticsTokenFromAuthBackend(@NonNull AuthBackend backend) {
        switch (backend) {
            case PASSWORD:
                return Analytics.Values.PASSWORD;
            case FACEBOOK:
                return Analytics.Values.FACEBOOK;
            case GOOGLE:
                return Analytics.Values.GOOGLE;
            default:
                throw new IllegalArgumentException(backend.name());
        }
    }

    //TTA

    @Nullable
    public void setCurrentUserProfileInCache(ProfileModel model) {
        if (model == null)
            return;
        if (model.name == null || model.name.equals(""))
            model.name = getCurrentUserProfile().name;

        if (model.email == null || model.email.equals(""))
            model.email = getCurrentUserProfile().email;

        //user name can't be modified
        model.username = getUsername();

        pref.put(PrefManager.Key.PROFILE_JSON, gson.toJson(model));
    }

    //wordpress
    public void storeWPAuthTokenResponse(@NonNull WpAuthResponse response) {

        clearWPAuthTokenResponse();
        pref.put(PrefManager.Key.WP_AUTH_JSON, gson.toJson(response));
    }

    public void clearWPAuthTokenResponse() {
        pref.put(PrefManager.Key.WP_AUTH_JSON, null);
    }

    public void storeWPRefreshTokenResponse(@NonNull WpAuthResponse refreshTokenResponse) {
        pref.put(PrefManager.Key.WP_AUTH_JSON, null);
        pref.put(PrefManager.Key.WP_AUTH_JSON, gson.toJson(refreshTokenResponse));
    }

    @Nullable
    public String getWPAuthorizationHeader() {
        final AuthResponse auth = getWPCurrentAuth();
        if (auth == null || !auth.isSuccess()) {
            // this might be a login with Facebook or Google
            return getSocialLoginAccessToken();
        } else {
            return String.format("%s %s", auth.token_type, auth.access_token);
        }
    }

    //region wordpress Auth user handeling
    @Nullable
    public AuthResponse getWPCurrentAuth() {
        final String json = pref.getString(PrefManager.Key.WP_AUTH_JSON);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, AuthResponse.class);
    }

    @Nullable
    public void setWPCurrentUserProfileInCache(WPProfileModel model) {
        if (model == null)
            return;

        pref.put(PrefManager.Key.WP_PROFILE_JSON, gson.toJson(model));
    }

    @Nullable
    public String getWPUsername() {
        final WPProfileModel profileModel = getWPCurrentUserProfile();
        return null == profileModel ? null : profileModel.username;
    }

    @Nullable
    public List<String> getWPUserRole() {
        final WPProfileModel profileModel = getWPCurrentUserProfile();
        return null == profileModel ? null : profileModel.roles;
    }

    @Nullable
    public WPProfileModel getWPCurrentUserProfile() {
        final String json = pref.getString(PrefManager.Key.WP_PROFILE_JSON);
        if (json == null) {
            return null;
        }

        return gson.fromJson(json, WPProfileModel.class);

    }
    //endregion

    //mx::Arjun shareds pref

    public void storeMxConnectCookie(@NonNull String connectCookie) {
        pref.put(PrefManager.Key.USER_CONNECT_LOGIN_COOKIE, connectCookie);
    }

    public void storeMxConnectCookieTimeStamp(@NonNull String connectCookieTimeStamp) {
        pref.put(PrefManager.Key.USER_CONNECT_LOGIN_COOKIE_TIME_STAMP, connectCookieTimeStamp);
    }

    @Nullable
    public String getMxConnectCookie() {
        return pref.getString(PrefManager.Key.USER_CONNECT_LOGIN_COOKIE);
    }

    @Nullable
    public String getMxConnectCookieTimeStamp() {
        return pref.getString(PrefManager.Key.USER_CONNECT_LOGIN_COOKIE_TIME_STAMP);
    }

    @Nullable
    public String getMxUserCourseAnswerData() {
        return pref.getString(PrefManager.Key.USER_COURSE_ANSWER_DATA);
    }

    public void storeMxUserCourseAnswerData(String mCourseAnswerData) {
        pref.put(PrefManager.Key.USER_COURSE_ANSWER_DATA, null);
        pref.put(PrefManager.Key.USER_COURSE_ANSWER_DATA, mCourseAnswerData);
    }

    public void setUserDataMigrationStatus() {
        pref.put(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS, null);
        pref.put(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS, "1");
    }

    public boolean getUserDataMigrationStatus() {
        boolean status = false;
        if (pref.getString(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS) != null &&
                !pref.getString(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS).equals("")) {
            if (pref.getString(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS).equals("1"))
                status = true;
        }
        return status;
    }

    public void setUserAppMinMigrationStatus() {
        pref.put(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS, null);
        pref.put(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS, "1");
    }

    public boolean geUserAppMinMigrationStatus() {
        boolean status = false;
        if (pref.getString(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS) != null &&
                !pref.getString(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS).equals("")) {
            if (pref.getString(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS).equals("1"))
                status = true;
        }
        return status;
    }

    public void removeMxUserCourseAnswerData() {
        pref.put(PrefManager.Key.USER_COURSE_ANSWER_DATA, null);
    }

    @Nullable
    public String getMxCurrentRegistrationFragName() {
        return pref.getString(PrefManager.Key.USER_CURRENT_FRAG_NAME_DATA);
    }

    public void setMxCurrentRegistrationFragName(String mFragName) {
        pref.put(PrefManager.Key.USER_CURRENT_FRAG_NAME_DATA, null);
        pref.put(PrefManager.Key.USER_CURRENT_FRAG_NAME_DATA, mFragName);
    }

    public void removeMxCurrentRegistrationFragName() {
        pref.put(PrefManager.Key.USER_CURRENT_FRAG_NAME_DATA, null);
    }


    public void storeCurrentDownloadingScromInfo(String mDownloadingScromInfo) {
        //store it in "meta::page::action" format
        pref.put(PrefManager.Key.USER_CURRENTDOWNLOADING_SCROM_INFO, null);
        pref.put(PrefManager.Key.USER_CURRENTDOWNLOADING_SCROM_INFO, mDownloadingScromInfo);
    }

    @Nullable
    public String getCurrentDownloadingScromInfo() {
        //get it in "meta::page" format
        return pref.getString(PrefManager.Key.USER_CURRENTDOWNLOADING_SCROM_INFO);
    }

    public void removeCurrentDownloadingScromInfo() {
        pref.put(PrefManager.Key.USER_CURRENTDOWNLOADING_SCROM_INFO, null);
    }

    //save Update profile info for managing view state
    @Nullable
    public String getMxProfileUpdatePageNameFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_NAME_INFO);
    }

    public void setMxProfileUpdatePageNameFieldInfo(String mName) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_NAME_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_NAME_INFO, mName);
    }

    @Nullable
    public String getMxProfileUpdatePageGenderFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_GENDER_INFO);
    }

    public void setMxProfileUpdatePageGenderFieldInfo(String mState) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_GENDER_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_GENDER_INFO, mState);
    }

    @Nullable
    public String getMxProfileUpdatePagePMISCodeFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_PMIS_CODE_INFO);
    }

    public void setMxProfileUpdatePagePMISCodeFieldInfo(String mState) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_PMIS_CODE_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_PMIS_CODE_INFO, mState);
    }

    @Nullable
    public String getMxProfileUpdatePageStateFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_STATE_INFO);
    }

    public void setMxProfileUpdatePageStateFieldInfo(String mState) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_STATE_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_STATE_INFO, mState);
    }


    @Nullable
    public String getMxProfileUpdatePageDistrictFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DISTRICT_INFO);
    }

    public void setMxProfileUpdatePageDistrictFieldInfo(String mDistrict) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DISTRICT_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DISTRICT_INFO, mDistrict);
    }

    @Nullable
    public String getMxProfileUpdatePageDIETFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DIET_INFO);
    }

    public void setMxProfileUpdatePageDIETFieldInfo(String mDiet) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DIET_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DIET_INFO, mDiet);
    }

    @Nullable
    public String getMxProfileUpdatePageBlockFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_BLOCK_INFO);
    }

    @Nullable
    public String getMxProfileUpdatePageTitleFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_TITLE_INFO);
    }

    public void setMxProfileUpdatePageBlockFieldInfo(String mBlock) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_BLOCK_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_BLOCK_INFO, mBlock);
    }

    public void setMxProfileUpdatePageTitleFieldInfo(String mTitle) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_TITLE_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_TITLE_INFO, mTitle);
    }

    @Nullable
    public String getMxProfileUpdatePageClassesTaughtFieldInfo() {
        return pref.getString(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_CLASSES_TAUGHT_INFO);
    }

    public void setMxProfileUpdatePageClassesTaughtFieldInfo(String mclasses_taught) {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_CLASSES_TAUGHT_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_CLASSES_TAUGHT_INFO, mclasses_taught);
    }

    //remove all cash for profile update page
    public void removeMxProfilePageCache() {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_NAME_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_GENDER_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_PMIS_CODE_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_CLASSES_TAUGHT_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DIET_INFO, null);

        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_STATE_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DISTRICT_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_BLOCK_INFO, null);
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_TITLE_INFO, null);
    }

    public void removeMxProfilePMISCodeCache() {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_PMIS_CODE_INFO, null);
    }

    public void removeMxProfileTitleCache() {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_TITLE_INFO, null);
    }

    public void removeMxProfilePageStateCache() {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_STATE_INFO, null);
    }

    public void removeMxProfilePageDistrictCache() {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_DISTRICT_INFO, null);
    }

    public void removeMxProfilePageBlockCache() {
        pref.put(PrefManager.Key.USER_PROFIILE_UPDATE_PAGE_BLOCK_INFO, null);
    }


    public void setCourseDashboardLoaded(boolean isLoaded) {
        pref.put(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED, null);
        if (isLoaded == true) {
            pref.put(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED, "true");
        } else {
            pref.put(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED, "false");
        }
    }

    public boolean isCourseDashboardLoaded() {
        boolean is_loaded = false;

        if (pref.getString(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED) != null &&
                pref.getString(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED).toLowerCase().equals("true")) {
            is_loaded = true;
        }
        return is_loaded;
    }

    @Nullable
    public FieldInfo getMxGenericFieldInfo() {
        final String json = pref.getString(PrefManager.Key.USER_GENERIC_FIELD_ATTRIBUTE_INFO);
        if (null == json) {
            return null;
        }
        return gson.fromJson(json, FieldInfo.class);
    }

    public void setMxGenericFieldInfo(FieldInfo fieldInfo) {
        if (fieldInfo == null && fieldInfo.getStateCustomAttribute() == null &&
                fieldInfo.getStateCustomAttribute().size() == 0)
            return;

        pref.put(PrefManager.Key.USER_GENERIC_FIELD_ATTRIBUTE_INFO, null);
        pref.put(PrefManager.Key.USER_GENERIC_FIELD_ATTRIBUTE_INFO, gson.toJson(fieldInfo));
    }

    public void setProgramFilters(@NonNull List<ProgramFilter> programFilters) {
        pref.put(PrefManager.Key.PROGRAM_FILTERS_JSON, gson.toJson(programFilters));
    }

    @Nullable
    public List<ProgramFilter> getProgramFilters() {
        final String json = pref.getString(PrefManager.Key.PROGRAM_FILTERS_JSON);
        if (null == json) {
            return null;
        }

        Type collectionType = new TypeToken<List<ProgramFilter>>() {
        }.getType();
        return gson.fromJson(json, collectionType);
    }

    public void setProgramId(String programId) {
        pref.put(PrefManager.Key.PROGRAM_ID, programId);
    }

    public String getProgramId() {
        return pref.getString(PrefManager.Key.PROGRAM_ID);
    }

    public void setProgramTitle(String programTitle) {
        pref.put(PrefManager.Key.PROGRAM_TITLE, programTitle);
    }

    public String getProgramTitle() {
        return pref.getString(PrefManager.Key.PROGRAM_TITLE);
    }

    public void setSectionId(String sectionId) {
        pref.put(PrefManager.Key.SECTION_ID, sectionId);
    }

    public String getSectionId() {
        return pref.getString(PrefManager.Key.SECTION_ID);
    }

    public void storeLatestAppInfo(@NonNull UpdateResponse res) {
        pref.put(PrefManager.Key.LATEST_APP_INFO_JSON, gson.toJson(res));
    }

    public String getRole() {
        return pref.getString(PrefManager.Key.ROLE);
    }

    public void setRole(String role) {
        pref.put(PrefManager.Key.ROLE, role);
    }

    public String getParentId() {
        return pref.getString(PrefManager.Key.parentId);
    }

    public void setParentId(String parentId) {
        pref.put(PrefManager.Key.parentId, parentId);
    }


    public String getLoginUserCookie() {
        return pref.getString(PrefManager.Key.LOGINUSERCOOKIE);
    }

    public void setLoginUserCookie(String login_user_cookie) {
        pref.put(PrefManager.Key.LOGINUSERCOOKIE, login_user_cookie);
    }

    public void setSessionFilter(String type) {
        pref.put(PrefManager.Key.SESSION_FILTER, type);
    }

    public String getSessionFilter() {
        return pref.getString(PrefManager.Key.SESSION_FILTER);
    }

    //    public void storeSessionFilter(@NonNull ProgramFilter filter) {
//        pref.put(PrefManager.Key.SESSION_FILTER, gson.toJson(filter));
//    }
//
//    @Nullable
//    public ProgramFilter getStoreSessionFilter() {
//        final String json = pref.getString(PrefManager.Key.SESSION_FILTER);
//        if (json == null) {
//            return null;
//        }
//        return gson.fromJson(json, ProgramFilter.class);
//    }
//    public void clearSessionSelectedFilterOption() {
//        pref.put(PrefManager.Key.SESSION_FILTER, null);
//    }
//
    public void storeSessionFilterTag(@NonNull ProgramFilterTag filter) {
        pref.put(PrefManager.Key.SESSION_FILTER_TAG, gson.toJson(filter));
    }

    @Nullable
    public ProgramFilterTag getStoreSessionFilterTag() {
        final String json = pref.getString(PrefManager.Key.SESSION_FILTER_TAG);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, ProgramFilterTag.class);
    }

    public void clearSessionSelectedFilterOptionTag() {
        pref.put(PrefManager.Key.SESSION_FILTER_TAG, null);
    }
//

    public void storeProgramFilter(@NonNull ProgramFilter filter) {
        pref.put(PrefManager.Key.PROGRAM_FILTER, gson.toJson(filter));
    }

    @Nullable
    public ProgramFilter getstoreProgramFilter() {
        final String json = pref.getString(PrefManager.Key.PROGRAM_FILTER);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, ProgramFilter.class);
    }

    public void setLangTag(@NonNull String tag) {
        pref.put(PrefManager.Key.Lang_FILTER_TAG, tag);
    }

    @Nullable
    public String getLangTag() {
        return pref.getString(PrefManager.Key.Lang_FILTER_TAG);
    }

    public void storeTags(@NonNull List<ProgramFilterTag> filter) {
        pref.put(PrefManager.Key.TAG_LIST, gson.toJson(filter));
    }

    @Nullable
    public List<ProgramFilterTag> getTags() {
        final String json = pref.getString(PrefManager.Key.TAG_LIST);
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<List<ProgramFilterTag>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    public void clearTags() {
        editor.remove("PrefManager.Key.TAG_LIST");
        pref.put(PrefManager.Key.TAG_LIST, gson.toJson(null));
    }

    public void setTypeFilter(String type) {
        pref.put(PrefManager.Key.TYPEFILTER, type);
    }

    public String getTypeFilter() {
        return pref.getString(PrefManager.Key.TYPEFILTER);
    }

    public void setPeriodFilter(String type) {
        pref.put(PrefManager.Key.PERIOD_FILTER, type);
    }

    public String getPeriodFilter() {
        return pref.getString(PrefManager.Key.PERIOD_FILTER);
    }

    public void clearAllFilters() {
        pref.put(PrefManager.Key.SESSION_FILTER, null);
        pref.put(PrefManager.Key.SESSION_FILTER_TAG, null);
        pref.put(PrefManager.Key.SESSION_FILTER_TAG, null);
        pref.put(PrefManager.Key.PROGRAM_FILTER, null);
        pref.put(PrefManager.Key.Lang_FILTER_TAG, null);
        pref.put(PrefManager.Key.TYPEFILTER, null);
        pref.put(PrefManager.Key.PERIOD_FILTER, null);
        pref.put(PrefManager.Key.TAG_LIST, gson.toJson(null));
    }

    public void setCurrrentPeriod(long periodId) {
        pref.put(PrefManager.Key.CURRENT_PERIOD_ID, periodId);
    }

    public long getCurrrentPeriod() {
        return pref.getLong(PrefManager.Key.CURRENT_PERIOD_ID);
    }

    public void setCurrrentPeriodTitle(String periodId) {
        pref.put(PrefManager.Key.CURRENT_PERIOD_TITLE, periodId);
    }

    public String getCurrrentPeriodTitle() {
        return pref.getString(PrefManager.Key.CURRENT_PERIOD_TITLE);
    }
}
