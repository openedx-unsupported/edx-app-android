package org.edx.mobile.module.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InfoPrefs @Inject constructor(
    @ApplicationContext context: Context
) : PrefBaseManager(context, INFO) {

    var appVersionCode: Long
        get() = getLong(APP_VERSION_CODE)
        set(code) = put(APP_VERSION_CODE, code)

    var appVersionName: String
        get() = getString(APP_VERSION_NAME, "0.0.0")
        set(code) = put(APP_VERSION_NAME, code)

    var appRating: Float
        get() = getFloat(APP_RATING)
        set(appRating) = put(APP_RATING, appRating)

    var lastRatedVersion: String
        get() = getString(LAST_RATED_VERSION, "0.0.0")
        set(versionName) = put(LAST_RATED_VERSION, versionName)

    var whatsNewShownVersion: String?
        get() = getString(WHATS_NEW_SHOWN_FOR_VERSION)
        set(version) = put(WHATS_NEW_SHOWN_FOR_VERSION, version)

    companion object {
        private const val APP_VERSION_NAME = "app_version_name"
        private const val APP_VERSION_CODE = "app_version_code"
        private const val APP_RATING = "APP_RATING"
        private const val LAST_RATED_VERSION = "LAST_RATED_VERSION"
        private const val WHATS_NEW_SHOWN_FOR_VERSION = "WHATS_NEW_SHOWN_FOR_VERSION"
    }
}
