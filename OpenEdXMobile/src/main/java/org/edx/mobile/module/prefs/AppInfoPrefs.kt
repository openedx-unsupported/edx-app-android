package org.edx.mobile.module.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.view.BulkDownloadFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInfoPrefs @Inject constructor(@ApplicationContext context: Context) :
    PrefBaseManager(context, Pref.APP_INFO) {
    init {
        migrateData(PrefBaseManager(context, Pref.COURSE_CALENDAR_PREF))
        migrateData(PrefBaseManager(context, Pref.VIDEOS))
    }

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

    fun setSyncAlertPopupDisabled(courseName: String) {
        put(courseName.replace(" ", "_"), true)
    }

    fun isSyncAlertPopupDisabled(courseName: String): Boolean =
        getBoolean(courseName.replace(" ", "_"), false)

    fun getBulkDownloadSwitchState(courseId: String?): BulkDownloadFragment.SwitchState {
        val ordinal = getInt(
            String.format(BULK_DOWNLOAD_FOR_COURSE_ID, courseId),
            BulkDownloadFragment.SwitchState.DEFAULT.ordinal
        )
        return BulkDownloadFragment.SwitchState.values()[ordinal]
    }

    fun setBulkDownloadSwitchState(
        state: BulkDownloadFragment.SwitchState,
        courseId: String?
    ) {
        put(String.format(BULK_DOWNLOAD_FOR_COURSE_ID, courseId), state.ordinal)
    }


    companion object {
        private const val APP_VERSION_NAME = "app_version_name"
        private const val APP_VERSION_CODE = "app_version_code"
        private const val APP_RATING = "APP_RATING"
        private const val LAST_RATED_VERSION = "LAST_RATED_VERSION"
        private const val WHATS_NEW_SHOWN_FOR_VERSION = "WHATS_NEW_SHOWN_FOR_VERSION"
        private const val BULK_DOWNLOAD_FOR_COURSE_ID = "BULK_DOWNLOAD_%s"
    }
}
