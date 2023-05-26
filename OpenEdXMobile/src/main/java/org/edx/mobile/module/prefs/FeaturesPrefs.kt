package org.edx.mobile.module.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.BuildConfig
import org.edx.mobile.model.api.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturesPrefs @Inject constructor(
    @ApplicationContext context: Context
) : PrefBaseManager(context, FEATURES) {

    var appConfig: AppConfig
        get() = getString(APP_CONFIG)?.let {
            gson.fromJson(it, AppConfig::class.java)
        } ?: AppConfig()
        set(value) = put(APP_CONFIG, gson.toJson(value))

    private val iapConfig
        get() = appConfig.iapConfig

    val isValuePropEnabled
        get() = appConfig.isValuePropEnabled

    val isIAPEnabled
        get() = iapConfig.isEnabled &&
                iapConfig.disableVersions.contains(BuildConfig.VERSION_NAME).not()

    val isIAPExperimentEnabled
        get() = isIAPEnabled && iapConfig.isExperimentEnabled

    var canAutoCheckUnfulfilledPurchase: Boolean
        get() = getBoolean(CHECK_UNFULFILLED_PURCHASE, false)
        set(value) = put(CHECK_UNFULFILLED_PURCHASE, value)

    /**
     * Method to check if the IAP is enabled for treatment/control group
     * Any user with odd user Id falls under treatment group and
     * user with even id falls under control group
     * The App will allow the user to purchase the course only if [org.edx.mobile.model.api.IAPConfig.isEnabled] is true
     * If [org.edx.mobile.model.api.IAPConfig.isExperimentEnabled] is true then only treatment group will be able to buy the course.
     */
    fun isIAPEnabledForUser(isOddUserId: Boolean): Boolean {
        if (isIAPEnabled) {
            if (isIAPExperimentEnabled) {
                return isOddUserId
            }
            return true
        }
        return false
    }

    companion object {
        // Preference to save app config
        private const val APP_CONFIG = "APP_CONFIG"
        private const val CHECK_UNFULFILLED_PURCHASE = "CHECK_UNFULFILLED_PURCHASE"
    }
}
