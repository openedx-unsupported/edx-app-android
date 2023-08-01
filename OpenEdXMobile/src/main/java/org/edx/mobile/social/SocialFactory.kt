package org.edx.mobile.social

import android.app.Activity
import org.edx.mobile.social.facebook.FacebookAuth
import org.edx.mobile.social.google.GoogleOauth2
import org.edx.mobile.social.microsoft.MicrosoftAuth
import org.edx.mobile.util.Config

object SocialFactory {

    enum class SocialSourceType(
        private val values: List<String>
    ) {
        UNKNOWN(listOf("unknown")),
        FACEBOOK(listOf("facebook")),
        GOOGLE(listOf("google-oauth2", "google")),
        MICROSOFT(listOf("azuread-oauth2", "azuread"));

        companion object {
            fun fromString(source: String?): SocialSourceType {
                return when (source?.lowercase()) {
                    in FACEBOOK.values -> FACEBOOK
                    in GOOGLE.values -> GOOGLE
                    in MICROSOFT.values -> MICROSOFT
                    else -> UNKNOWN
                }
            }
        }
    }

    @JvmStatic
    fun getInstance(activity: Activity, type: SocialSourceType, config: Config): ISocial {
        if (isSocialFeatureEnabled(type, config)) {
            return when (type) {
                SocialSourceType.GOOGLE -> GoogleOauth2(activity)
                SocialSourceType.FACEBOOK -> FacebookAuth(activity)
                SocialSourceType.MICROSOFT -> MicrosoftAuth(activity)
                SocialSourceType.UNKNOWN -> ISocialEmptyImpl()
            }
        }
        return ISocialEmptyImpl()
    }

    fun isSocialFeatureEnabled(
        type: SocialSourceType,
        config: Config
    ): Boolean {
        return when (type) {
            SocialSourceType.GOOGLE -> config.googleConfig.isEnabled
            SocialSourceType.FACEBOOK -> config.facebookConfig.isEnabled
            SocialSourceType.MICROSOFT -> config.microsoftConfig.isEnabled
            else -> true
        }
    }
}
