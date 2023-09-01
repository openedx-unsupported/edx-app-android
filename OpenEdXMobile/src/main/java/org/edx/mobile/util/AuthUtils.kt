package org.edx.mobile.util

import android.app.Activity
import org.edx.mobile.R
import org.edx.mobile.authentication.LoginAPI
import org.edx.mobile.exception.LoginErrorMessage
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.social.SocialLoginDelegate
import java.net.HttpURLConnection

object AuthUtils {

    fun getLoginErrorMessage(
        activity: Activity,
        config: Config,
        backend: String?,
        feature: SocialLoginDelegate.Feature,
        e: LoginAPI.AccountNotLinkedException,
    ): LoginErrorMessage {
        val isFacebook = backend.equals(LoginPrefs.BACKEND_FACEBOOK, ignoreCase = true)
        val isMicrosoft = backend.equals(LoginPrefs.BACKEND_MICROSOFT, ignoreCase = true)
        if (feature == SocialLoginDelegate.Feature.SIGN_IN && e.responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            val title = activity.resources.getString(R.string.login_error)
            val desc = ResourceUtil.getFormattedString(
                activity.resources,
                if (isFacebook) {
                    R.string.error_account_not_linked_desc_fb_2
                } else if (isMicrosoft) {
                    R.string.error_account_not_linked_desc_microsoft_2
                } else {
                    R.string.error_account_not_linked_desc_google_2
                },
                AppConstants.PLATFORM_NAME, config.platformName
            )
            return LoginErrorMessage(title, desc.toString())
        }

        val titleResId = when {
            isFacebook -> R.string.error_account_not_linked_title_fb
            isMicrosoft -> R.string.error_account_not_linked_title_microsoft
            else -> R.string.error_account_not_linked_title_google
        }
        val title = ResourceUtil.getFormattedString(
            activity.resources,
            titleResId,
            AppConstants.PLATFORM_NAME, config.platformName
        )

        val descResId = when {
            isFacebook -> R.string.error_account_not_linked_desc_fb
            isMicrosoft -> R.string.error_account_not_linked_desc_microsoft
            else -> R.string.error_account_not_linked_desc_google
        }

        val descParamsDesc = HashMap<String, CharSequence>()
        descParamsDesc[AppConstants.PLATFORM_NAME] = config.platformName
        descParamsDesc[AppConstants.PLATFORM_DESTINATION] = config.platformDestinationName

        val desc = ResourceUtil.getFormattedString(
            activity.resources,
            descResId,
            descParamsDesc
        )

        return LoginErrorMessage(title.toString(), desc.toString())
    }
}
