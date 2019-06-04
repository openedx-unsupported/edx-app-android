package org.edx.mobile.module.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.services.EdxCookieManager;
import org.edx.mobile.tta.data.model.authentication.FieldInfo;
import org.edx.mobile.tta.wordpress_client.model.WPProfileModel;
import org.edx.mobile.tta.wordpress_client.model.WpAuthResponse;
import org.edx.mobile.user.ProfileImage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPrefs {

    public enum AuthBackend {
        PASSWORD,
        FACEBOOK,
        GOOGLE
    }

    @NonNull
    private final Gson gson = new GsonBuilder().create();

    @NonNull
    private final PrefManager pref;

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
        if(model==null)
            return;
        if(model.name==null || model.name.equals(""))
            model.name=getCurrentUserProfile().name;

        if(model.email==null || model.email.equals(""))
            model.email=getCurrentUserProfile().email;

        //user name can't be modified
        model.username=getUsername();

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
        if(model==null)
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
        boolean status=false;
        if(pref.getString(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS)!=null &&
                ! pref.getString(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS).equals(""))
        {
            if(pref.getString(PrefManager.Key.USER_OLD_DATA_MIGRATION_STATUS).equals("1"))
                status=true;
        }
        return status;
    }

    public void setUserAppMinMigrationStatus() {
        pref.put(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS, null);
        pref.put(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS, "1");
    }

    public boolean geUserAppMinMigrationStatus() {
        boolean status=false;
        if(pref.getString(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS)!=null &&
                ! pref.getString(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS).equals(""))
        {
            if(pref.getString(PrefManager.Key.USER_APP_MIN_MIGRATION_STATUS).equals("1"))
                status=true;
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
        if(isLoaded==true)
        {
            pref.put(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED, "true");
        }
        else
        {
            pref.put(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED, "false");
        }
    }

    public boolean isCourseDashboardLoaded() {
        boolean is_loaded=false;

        if(pref.getString(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED) !=null &&
                pref.getString(PrefManager.Key.IS_USER_COURSE_DASHBOARD_LOADED).toLowerCase().equals("true"))
        {
            is_loaded=true;
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
        if(fieldInfo==null && fieldInfo.getStateCustomAttribute()==null &&
                fieldInfo.getStateCustomAttribute().size()==0)
            return;

        pref.put(PrefManager.Key.USER_GENERIC_FIELD_ATTRIBUTE_INFO, null);
        pref.put(PrefManager.Key.USER_GENERIC_FIELD_ATTRIBUTE_INFO, gson.toJson(fieldInfo));
    }
}
