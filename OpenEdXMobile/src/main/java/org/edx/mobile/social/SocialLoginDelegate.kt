package org.edx.mobile.social

import android.app.Activity
import android.content.Intent
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.edx.mobile.R
import org.edx.mobile.logger.Logger
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.social.facebook.FacebookAuth
import org.edx.mobile.social.facebook.FacebookProvider
import org.edx.mobile.social.google.GoogleOauth2
import org.edx.mobile.social.google.GoogleProvider
import org.edx.mobile.social.microsoft.MicrosoftAuth
import org.edx.mobile.social.microsoft.MicrosoftProvide
import org.edx.mobile.util.Config
import org.edx.mobile.util.ConfigUtil.Companion.isSocialFeatureEnabled
import org.edx.mobile.util.NetworkUtil
import org.edx.mobile.view.ICommonUI

/**
 * Code refactored from Login Activity, for the logic of login to social site are the same
 * for both login and registration.
 */
class SocialLoginDelegate(
    private val activity: Activity,
    private val callback: MobileLoginCallback, config: Config,
    private val loginPrefs: LoginPrefs,
    private val feature: Feature,
) {
    private val logger = Logger(javaClass.name)

    enum class Feature {
        SIGN_IN, REGISTRATION
    }

    private val google: ISocial
    private val facebook: ISocial
    private val microsoft: ISocial

    init {
        google = getInstance(SocialAuthSource.GOOGLE, config)
        google.setCallback { accessToken: String ->
            logger.debug("Google logged in; token= $accessToken")
            onSocialLoginSuccess(accessToken, LoginPrefs.BACKEND_GOOGLE)
        }
        facebook = getInstance(SocialAuthSource.FACEBOOK, config)
        facebook.setCallback { accessToken: String ->
            logger.debug("Facebook logged in; token= $accessToken")
            onSocialLoginSuccess(accessToken, LoginPrefs.BACKEND_FACEBOOK)
        }
        microsoft = getInstance(SocialAuthSource.MICROSOFT, config)
        microsoft.setCallback(object : ISocial.Callback {
            override fun onCancel() {
                (activity as? ICommonUI)?.tryToSetUIInteraction(true)
            }

            override fun onError(exception: Exception?) {
                (activity as? ICommonUI)?.tryToSetUIInteraction(true)
            }

            override fun onLogin(accessToken: String) {
                logger.debug("Microsoft logged in; token= $accessToken")
                onSocialLoginSuccess(accessToken, LoginPrefs.BACKEND_MICROSOFT)
            }
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        google.onActivityResult(requestCode, resultCode, data)
        facebook.onActivityResult(requestCode, resultCode, data)
        microsoft.onActivityResult(requestCode, resultCode, data)
    }

    private fun socialLogin(socialAuthSource: SocialAuthSource) {
        when (socialAuthSource) {
            SocialAuthSource.FACEBOOK -> facebook.login()
            SocialAuthSource.GOOGLE -> google.login()
            SocialAuthSource.MICROSOFT -> microsoft.login()
            SocialAuthSource.UNKNOWN -> {}
        }
    }

    private fun socialLogout(socialAuthSource: SocialAuthSource) {
        when (socialAuthSource) {
            SocialAuthSource.FACEBOOK -> facebook.logout()
            SocialAuthSource.GOOGLE -> google.logout()
            SocialAuthSource.MICROSOFT -> microsoft.logout()
            else -> {}
        }
    }

    /**
     * called with you to use social login
     *
     * @param accessToken
     * @param backend
     */
    fun onSocialLoginSuccess(accessToken: String, backend: String) {
        loginPrefs.saveSocialLoginToken(accessToken, backend)
        callback.performUserLogin(accessToken, backend, feature)
    }

    fun getUserInfo(
        socialAuthSource: SocialAuthSource,
        accessToken: String,
        userInfoCallback: SocialUserInfoCallback
    ) {
        var socialProvider: SocialProvider? = null
        if (socialAuthSource === SocialAuthSource.FACEBOOK) {
            socialProvider = FacebookProvider()
        } else if (socialAuthSource === SocialAuthSource.GOOGLE) {
            socialProvider = GoogleProvider(google as GoogleOauth2)
        } else if (socialAuthSource === SocialAuthSource.MICROSOFT) {
            socialProvider = MicrosoftProvide()
        }
        socialProvider?.getUserInfo(activity, accessToken, userInfoCallback)
    }

    fun getInstance(source: SocialAuthSource?, config: Config?): ISocial {
        return if (isSocialFeatureEnabled(source!!, config!!)) {
            when (source) {
                SocialAuthSource.GOOGLE -> GoogleOauth2(activity)
                SocialAuthSource.FACEBOOK -> FacebookAuth(activity)
                SocialAuthSource.MICROSOFT -> MicrosoftAuth(activity)
                SocialAuthSource.UNKNOWN -> ISocialEmptyImpl()
            }
        } else ISocialEmptyImpl()
    }

    fun createSocialButtonClickHandler(socialAuthSource: SocialAuthSource): SocialButtonClickHandler {
        return SocialButtonClickHandler(socialAuthSource)
    }

    inner class SocialButtonClickHandler(
        private val socialAuthSource: SocialAuthSource,
    ) : View.OnClickListener {

        override fun onClick(v: View) {
            if (!NetworkUtil.isConnected(activity)) {
                callback.showAlertDialog(
                    activity.getString(R.string.no_connectivity),
                    activity.getString(R.string.network_not_connected)
                )
                return
            }

            (activity as? ICommonUI)?.tryToSetUIInteraction(false)
            CoroutineScope(Dispatchers.IO).launch {
                val response = kotlin.runCatching { socialLogout(socialAuthSource) }
                response.onSuccess {
                    withContext(Dispatchers.Main) {
                        try {
                            socialLogin(socialAuthSource)
                        } catch (ex: Exception) {
                            (activity as? ICommonUI)?.tryToSetUIInteraction(true)
                        }
                    }
                }.onFailure {
                    (activity as? ICommonUI)?.tryToSetUIInteraction(true)
                }
            }
        }
    }

    interface MobileLoginCallback {
        fun showAlertDialog(header: String?, message: String)
        fun performUserLogin(accessToken: String, backend: String, feature: Feature)
    }

    interface SocialUserInfoCallback {
        fun setSocialUserInfo(email: String?, name: String?)
    }
}
