package org.edx.mobile.module.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.model.api.ProfileModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrefs @Inject constructor(
    @ApplicationContext val context: Context, val loginPrefs: LoginPrefs
) {
    /**
     * Returns true if the "download over wifi only" is turned ON, false otherwise.
     *
     * @return
     */
    val isDownloadOverWifiOnly: Boolean
        get() {
            // check if download is only allowed over wifi
            val wifiPrefManager = PrefManager(context, PrefManager.Pref.WIFI)
            return wifiPrefManager.getBoolean(
                PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true
            )
        }

    val isDownloadToSDCardEnabled: Boolean
        get() {
            val prefManger = PrefManager(context, PrefManager.Pref.USER_PREF)
            return prefManger.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, false)
        }

    val profile: ProfileModel
        get() = loginPrefs.currentUserProfile
}
