package org.edx.mobile.module.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.R
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.util.VideoPlaybackSpeed
import org.edx.mobile.view.BulkDownloadFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrefs @Inject constructor(
    @ApplicationContext context: Context,
    loginPrefs: LoginPrefs
) : PrefBaseManager(context, USER) {

    init {
        migrateData(object : PrefBaseManager(context, WIFI) {})
        migrateData(object : PrefBaseManager(context, COURSE_CALENDAR_PREF) {})
        migrateData(object : PrefBaseManager(context, VIDEOS) {})

        loginPrefs.getString(TRANSCRIPT_LANGUAGE)?.let {
            put(TRANSCRIPT_LANGUAGE, it)
            loginPrefs.removeKey(TRANSCRIPT_LANGUAGE)
        }
        if (loginPrefs.getFloat(PLAYBACK_SPEED) != -1f) {
            put(PLAYBACK_SPEED, loginPrefs.getFloat(PLAYBACK_SPEED))
            loginPrefs.removeKey(PLAYBACK_SPEED)
        }
        if (loginPrefs.getInt(VIDEO_QUALITY) != -1) {
            put(VIDEO_QUALITY, loginPrefs.getInt(VIDEO_QUALITY))
            loginPrefs.removeKey(VIDEO_QUALITY)
        }
        if (getInt(VIDEO_QUALITY) == -1) {
            put(VIDEO_QUALITY, VideoQuality.AUTO.ordinal)
        }
    }

    var isDownloadOverWifiOnly: Boolean
        get() = getBoolean(DOWNLOAD_ONLY_ON_WIFI, true)
        set(value) = put(DOWNLOAD_ONLY_ON_WIFI, value)

    var isDownloadToSDCardEnabled: Boolean
        get() = getBoolean(DOWNLOAD_TO_SDCARD, false)
        set(value) = put(DOWNLOAD_TO_SDCARD, value)

    var speedTestKBPS: Float
        get() = getFloat(SPEED_TEST_KBPS, 0.0f)
        set(value) = put(SPEED_TEST_KBPS, value)

    var subtitleLanguage: String
        get() {
            getString(TRANSCRIPT_LANGUAGE, NONE).let {
                return if (it == context.getString(R.string.lbl_cc_none)) {
                    subtitleLanguage = NONE
                    NONE
                } else it
            }
        }
        set(language) = put(TRANSCRIPT_LANGUAGE, language)

    var playbackSpeed: Float
        get() = getFloat(PLAYBACK_SPEED, VideoPlaybackSpeed.SPEED_1_0X.speedValue)
        set(speed) = put(PLAYBACK_SPEED, speed)

    var videoQuality: VideoQuality
        get() = VideoQuality.values()[getInt(VIDEO_QUALITY, VideoQuality.AUTO.ordinal)]
        set(quality) = put(VIDEO_QUALITY, quality.ordinal)

    fun setCalendarSyncViewEventAlertDisabled(courseName: String) {
        put(courseName.replace(" ", "_"), true)
    }

    fun isCalendarSyncViewEventAlertDisabled(courseName: String): Boolean =
        getBoolean(courseName.replace(" ", "_"), false)

    fun getBulkDownloadSwitchState(courseId: String?): BulkDownloadFragment.SwitchState {
        val key = String.format(BULK_DOWNLOAD_FOR_COURSE_ID, courseId)
        val ordinal = getInt(key, BulkDownloadFragment.SwitchState.DEFAULT.ordinal)
        return BulkDownloadFragment.SwitchState.values()[ordinal]
    }

    fun setBulkDownloadSwitchState(
        state: BulkDownloadFragment.SwitchState,
        courseId: String?
    ) {
        put(String.format(BULK_DOWNLOAD_FOR_COURSE_ID, courseId), state.ordinal)
    }

    val hasSubtitleLanguage: Boolean
        get() = subtitleLanguage.equals(NONE, true).not()

    companion object {
        private const val DOWNLOAD_ONLY_ON_WIFI = "download_only_on_wifi"
        private const val TRANSCRIPT_LANGUAGE = "transcript_language"
        private const val PLAYBACK_SPEED = "playback_speed"
        private const val VIDEO_QUALITY = "video_quality"
        private const val DOWNLOAD_TO_SDCARD = "download_to_sdcard"
        private const val SPEED_TEST_KBPS = "speed_test_kbps"
        private const val BULK_DOWNLOAD_FOR_COURSE_ID = "BULK_DOWNLOAD_%s"
        const val NONE = "NONE"
    }
}
