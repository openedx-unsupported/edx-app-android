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
import org.edx.mobile.user.ProfileImage;

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

    public void setYearOfBirth(@NonNull String username, @Nullable Integer yearOfBirth) {
        if (username.equals(getUsername())) {
            ProfileModel profileModel = getCurrentUserProfile();
            profileModel.year_of_birth = yearOfBirth;
            this.storeUserProfile(profileModel);
        }
    }

    @Nullable
    public Integer getYearOfBirth() {
        final ProfileModel profileModel = getCurrentUserProfile();
        return null == profileModel ? null : profileModel.year_of_birth;
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
}
