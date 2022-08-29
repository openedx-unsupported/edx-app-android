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
}
