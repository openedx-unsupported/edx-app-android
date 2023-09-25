package org.edx.mobile.module.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.base.MainApplication
import org.edx.mobile.model.api.ProfileModel
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.model.user.ProfileImage
import org.edx.mobile.services.EdxCookieManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginPrefs @Inject constructor(
    @ApplicationContext context: Context
) : PrefBaseManager(context, LOGIN) {

    var currentAuth: AuthResponse?
        get() {
            val jsonAuth = getString(AUTH_JSON) ?: return null
            return gson.fromJson(jsonAuth, AuthResponse::class.java)
        }
        set(response) = put(AUTH_JSON, gson.toJson(response))

    var authBackendType: String?
        get() = getString(ANALYTICS_KEY_BACKEND)
        set(backend) = put(ANALYTICS_KEY_BACKEND, backend)

    // this might be a login with Facebook or Google
    val authorizationHeader: String?
        get() {
            val auth = currentAuth
            return if (auth == null || !auth.isSuccess) {
                // this might be a login with Facebook or Google
                socialLoginAccessToken
            } else {
                String.format("%s %s", auth.token_type, auth.access_token)
            }
        }

    var socialLoginAccessToken: String?
        get() = getString(AUTH_TOKEN_SOCIAL)
        set(accessToken) = put(AUTH_TOKEN_SOCIAL, accessToken)

    var socialLoginProvider: String?
        get() = getString(AUTH_TOKEN_BACKEND)
        set(backend) = put(AUTH_TOKEN_BACKEND, backend)

    val isUserLoggedIn: Boolean
        get() = getString(PROFILE_JSON) != null

    private var jsonProfile: String?
        get() = getString(PROFILE_JSON)
        set(value) = put(PROFILE_JSON, value)

    /**
     * For an active session, the user profile is always non null but for verification purpose this
     * method should always be called after [isUserLoggedIn] method.
     *
     * @return Active User Session Profile Data
     */
    var currentUserProfile: ProfileModel
        get() = gson.fromJson(jsonProfile, ProfileModel::class.java)
        set(value) {
            jsonProfile = gson.toJson(value)
        }

    val userId: Long
        get() = currentUserProfile.id

    val isOddUserId: Boolean
        get() = userId % 2 == 1L

    val username: String
        get() = currentUserProfile.username

    val userEmail: String?
        get() = if (isUserLoggedIn) currentUserProfile.email else null

    var profileImage: ProfileImage?
        get() {
            val json = getString(PROFILE_IMAGE) ?: return null
            return gson.fromJson(json, ProfileImage::class.java)
        }
        set(value) = put(PROFILE_IMAGE, gson.toJson(value))

    var lastAuthenticatedEmail: String?
        get() = getString(AUTH_EMAIL)
        set(emailAddress) = put(AUTH_EMAIL, emailAddress)

    var alreadyRegisteredLoggedIn: Boolean
        get() = getBoolean(ALREADY_REGISTERED_BECAME_LOGGED_IN, false)
        set(isRegisteredAlready) = put(ALREADY_REGISTERED_BECAME_LOGGED_IN, isRegisteredAlready)

    fun storeAuthTokenResponse(response: AuthResponse, backend: AuthBackend) {
        currentAuth = response
        authBackendType = backend.value()
    }

    fun storeRefreshTokenResponse(refreshTokenResponse: AuthResponse) {
        currentAuth = refreshTokenResponse
    }

    fun saveSocialLoginToken(accessToken: String, backend: String) {
        socialLoginAccessToken = accessToken
        socialLoginProvider = backend
    }

    fun clearAuthTokenResponse() {
        currentAuth = null
        authBackendType = null
    }

    fun clearSocialLoginToken() {
        socialLoginAccessToken = null
        socialLoginProvider = null
    }

    fun storeUserProfile(profile: ProfileModel) {
        currentUserProfile = profile
        clearSocialLoginToken()
    }

    fun clearLogin() {
        clear()
        EdxCookieManager.getSharedInstance(MainApplication.instance()).clearAllCookies()
    }

    fun setUserInfo(
        username: String,
        email: String,
        profileImage: ProfileImage?,
        hasLimitedProfile: Boolean
    ) {
        if (username == this.username) {
            val currentProfile = currentUserProfile
            currentProfile.email = email
            currentProfile.hasLimitedProfile = hasLimitedProfile
            this.currentUserProfile = currentProfile
            this.profileImage = profileImage
        }
    }

    fun setProfileImage(username: String, profileImage: ProfileImage?) {
        if (username == this.username) {
            this.profileImage = profileImage
        }
    }

    enum class AuthBackend {
        PASSWORD, FACEBOOK, GOOGLE, MICROSOFT;

        fun value(): String {
            return when (this) {
                PASSWORD -> LoginPrefs.PASSWORD
                FACEBOOK -> LoginPrefs.FACEBOOK
                GOOGLE -> LoginPrefs.GOOGLE
                MICROSOFT -> LoginPrefs.MICROSOFT
            }
        }
    }

    companion object {
        private const val PASSWORD = "Password"
        private const val FACEBOOK = "Facebook"
        private const val GOOGLE = "Google"
        private const val MICROSOFT = "Microsoft"

        private const val PROFILE_JSON = "profile_json"
        private const val AUTH_JSON = "auth_json"
        private const val AUTH_EMAIL = "email"
        private const val PROFILE_IMAGE = "profile_image"
        private const val AUTH_TOKEN_SOCIAL = "facebook_token"
        private const val AUTH_TOKEN_BACKEND = "google_token"

        //This key is used to store the Auth type, either password or any social provider
        private const val ANALYTICS_KEY_BACKEND = "segment_backend"
        private const val ALREADY_REGISTERED_BECAME_LOGGED_IN =
            "already_registered_became_logged_in"

        const val BACKEND_FACEBOOK = "facebook"
        const val BACKEND_GOOGLE = "google-oauth2"
        const val BACKEND_MICROSOFT = "azuread-oauth2"
    }
}
