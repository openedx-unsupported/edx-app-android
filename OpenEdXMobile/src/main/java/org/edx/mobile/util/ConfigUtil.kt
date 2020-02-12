package org.edx.mobile.util

import android.text.TextUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.edx.mobile.BuildConfig
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
                    val courseUpgradeEnabled = firebaseRemoteConfig
                            .getBoolean(AppConstants.FirebaseConstants.REV_934_ENABLED)
                    // Check course upgrade enabled in firebase remote config
                    if (courseUpgradeEnabled) {
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
                    }
                    listener.onCourseUpgradeResult(false)
                }
            }
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
}
