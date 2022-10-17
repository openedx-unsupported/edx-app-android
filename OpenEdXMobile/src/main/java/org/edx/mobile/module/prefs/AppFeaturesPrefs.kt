package org.edx.mobile.module.prefs

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.model.api.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppFeaturesPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val pref: PrefManager = PrefManager(context, PrefManager.Pref.APP_FEATURES)

    fun isValuePropEnabled() = getAppConfig().isValuePropEnabled

    fun setAppConfig(appConfig: AppConfig) {
        pref.put(PrefManager.Key.APP_CONFIG, Gson().toJson(appConfig))
    }

    private fun getAppConfig(): AppConfig {
        return Gson().fromJson(pref.getString(PrefManager.Key.APP_CONFIG), AppConfig::class.java)
    }

    private fun getIAPConfig() = getAppConfig().iapConfig

    fun isIAPEnabled() = getIAPConfig().isEnabled

    fun isIAPExperimentEnabled() = isIAPEnabled() && getIAPConfig().isExperimentEnabled

    /**
     * Method to check if the IAP is enabled for treatment/control group
     * Any user with odd user Id falls under treatment group and
     * user with even id falls under control group
     * The App will allow the user to purchase the course only if [org.edx.mobile.model.api.IAPConfig.isEnabled] is true
     * If [org.edx.mobile.model.api.IAPConfig.isExperimentEnabled] is true then only treatment group will be able to buy the course.
     */
    fun isIAPEnabled(isOddUserId: Boolean): Boolean {
        if (isIAPEnabled()) {
            if (isIAPExperimentEnabled()) {
                return isOddUserId
            }
            return true
        }
        return false
    }
}
