package org.edx.mobile.util

import android.content.Context
import android.text.TextUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.edx.mobile.BuildConfig
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.model.CourseDatesCalendarSync
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException
import java.util.*


class ConfigUtil {
    companion object {
        /**
         * Returns true if domain of the given URL is white-listed in the configuration,
         * false otherwise.
         *
         * @param url
         * @return
         */
        fun isWhiteListedURL(url: String, config: Config): Boolean {
            // check if this URL is a white-listed URL, anything outside the white-list is EXTERNAL LINK
            for (domain in config.zeroRatingConfig.whiteListedDomains) {
                if (BrowserUtil.isUrlOfHost(url, domain)) {
                    // this is white-listed URL
                    return true
                }
            }
            return false
        }

        /**
         * Utility method to check the Course Discovery status
         *
         * @param environment       Relevant configuration environment.
         * @return true if Discovery enabled in configurations false else wise
         */
        fun isCourseDiscoveryEnabled(environment: IEdxEnvironment): Boolean {
            return environment.config.discoveryConfig?.courseDiscoveryConfig?.isDiscoveryEnabled == true
        }

        /**
         * Utility method to check the Degree Discovery status
         *
         * @param environment       Relevant configuration environment.
         * @return true if Degree Discovery enabled in configurations false else wise
         */
        fun isDegreeDiscoveryEnabled(environment: IEdxEnvironment): Boolean {
            return environment.config.discoveryConfig?.degreeDiscoveryConfig?.isDiscoveryEnabled(environment) == true
        }

        /**
         * Utility method to check the Course Webview Discovery status
         *
         * @param environment       Relevant configuration environment.
         * @return true if Course Webview Discovery enabled in configurations false else wise
         */
        fun isCourseWebviewDiscoveryEnabled(environment: IEdxEnvironment): Boolean {
            return environment.config.discoveryConfig?.courseDiscoveryConfig?.isWebviewDiscoveryEnabled == true
        }

        /**
         * Utility method to check the Program Discovery status
         *
         * @param environment       Relevant configuration environment.
         * @return true if Program Discovery enabled in configurations false else wise
         */
        fun isProgramDiscoveryEnabled(environment: IEdxEnvironment): Boolean {
            return environment.config.discoveryConfig?.programDiscoveryConfig?.isDiscoveryEnabled(environment) == true
        }

        /**
         * Utility method to check the Program Webview Discovery status
         *
         * @param environment       Relevant configuration environment.
         * @return true if Program Webview Discovery enabled in configurations false else wise
         */
        fun isProgramWebviewDiscoveryEnabled(environment: IEdxEnvironment): Boolean {
            return environment.config.discoveryConfig?.programDiscoveryConfig?.isWebviewDiscoveryEnabled == true
        }

        /**
         * Utility method to check the status of the course upgrade.
         *
         * @param config   [Config]
         * @param listener [OnCourseUpgradeStatusListener] callback for the status of the course upgrade.
         */
        fun checkCourseUpgradeEnabled(config: Config,
                                      listener: OnCourseUpgradeStatusListener) {
            // Check firebase enabled in config
            if (config.firebaseConfig.isEnabled) {
                val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
                firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
                    val whiteListedReleasesJson = firebaseRemoteConfig
                            .getString(AppConstants.FirebaseConstants.REV_934_WHITELISTED_RELEASES)
                    if (!TextUtils.isEmpty(whiteListedReleasesJson)) {
                        val whiteListedReleases = Gson().fromJson<ArrayList<String>>(whiteListedReleasesJson,
                                object : TypeToken<ArrayList<String>>() {}.type)
                        // Check current release is white listed in firebase remote config
                        for (release in whiteListedReleases) {
                            if (BuildConfig.VERSION_NAME.equals(release, ignoreCase = true)) {
                                listener.onCourseUpgradeResult(true)
                                return@addOnCompleteListener
                            }
                        }
                    }
                    listener.onCourseUpgradeResult(false)
                }
            }
        }

        /**
         * Utility method to check the status of the value prop.
         *
         * @param config   [Config]
         * @param listener [OnValuePropStatusListener] callback for the status of the value prop.
         */
        fun checkValuePropEnabled(config: Config,
                                  listener: OnValuePropStatusListener) {
            // Check firebase enabled in config
            if (config.firebaseConfig.isEnabled) {
                val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
                firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
                    val isEnabled: Boolean = firebaseRemoteConfig
                            .getBoolean(AppConstants.FirebaseConstants.VALUE_PROP_ENABLED)
                    listener.isValuePropEnabled(isEnabled)
                }
            }
        }

        /**
         * Utility method to check the response of the calendar sync.
         *
         * @param config   [Config]
         * @param listener [OnCalendarSyncListener] callback for the status of the value prop.
         */
        fun checkCalendarSyncEnabled(config: Config,
                                     listener: OnCalendarSyncListener) {
            // Check firebase enabled in config
            if (config.firebaseConfig.isEnabled) {
                val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
                firebaseRemoteConfig.fetch(0).addOnCompleteListener {
                    try {
                        val response = JSONObject(firebaseRemoteConfig
                                .getString(AppConstants.FirebaseConstants.COURSE_DATES_CALENDAR_SYNC))
                        val androidResponse = Gson().fromJson<CourseDatesCalendarSync>(response.getString(AppConstants.FirebaseConstants.KEY_ANDROID), CourseDatesCalendarSync::class.java)
                        listener.onCalendarSyncResponse(response = androidResponse)
                    } catch (e: InvocationTargetException) {
                        e.cause?.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        /**
         * Utility Method to get Agreement Urls based on [AgreementUrlType] and supported languages
         *
         * @param context   Current Context
         * @param config    [Config.AgreementUrlsConfig]
         * @param urlType   [AgreementUrlType] type of Url
         */
        @JvmStatic
        fun getAgreementUrl(
            context: Context,
            config: Config.AgreementUrlsConfig?,
            urlType: AgreementUrlType
        ): String? {
            if (config == null || TextUtils.isEmpty(config.getAgreementUrl(urlType))) {
                return context.resources.getString(urlType.getStringResId())
            }
            if (config.supportedLanguages != null && config.supportedLanguages.isNotEmpty()) {
                val currentLocal = LocaleUtils.getCurrentDeviceLanguage(context)
                if (config.supportedLanguages.contains(currentLocal)) {
                    return UrlUtil.appendPathAfterAuthority(
                        config.getAgreementUrl(urlType),
                        currentLocal
                    )
                }
            }
            return config.getAgreementUrl(urlType)
        }
    }

    /**
     * Interface to provide the callback for the status of the course upgrade.
     */
    interface OnCourseUpgradeStatusListener {
        /**
         * Callback to send course upgrade status result.
         */
        fun onCourseUpgradeResult(enabled: Boolean)
    }

    /**
     * Interface to provide the callback for the status of the value prop.
     */
    interface OnValuePropStatusListener {
        /**
         * Callback to send value prop status result.
         */
        fun isValuePropEnabled(isEnabled: Boolean)
    }

    /**
     * Interface to provide the callback for the response of the remote config for Calendar Sync.
     */
    interface OnCalendarSyncListener {
        /**
         * Callback to send response of Calendar sync.
         */
        fun onCalendarSyncResponse(response: CourseDatesCalendarSync)
    }
}
