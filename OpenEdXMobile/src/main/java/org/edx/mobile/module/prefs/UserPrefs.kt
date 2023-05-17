package org.edx.mobile.module.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.edx.mobile.R
import org.edx.mobile.model.video.VideoQuality
import org.edx.mobile.util.VideoPlaybackSpeed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrefs @Inject constructor(@ApplicationContext context: Context) :
    PrefBaseManager(context, Pref.USER_PREF) {
    private val loginPrefs: LoginPrefs = LoginPrefs(context)

    init {
        migrateData(PrefBaseManager(context, Pref.WIFI))
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
        set(value) = put(SPEED_TEST_KBPS, value);

    var subtitleLanguage: String
        get() {
            getString(TRANSCRIPT_LANGUAGE, DEFAULT_VALUE).let {
                return if (it == context.getString(R.string.lbl_cc_none)) {
                    subtitleLanguage = DEFAULT_VALUE
                    DEFAULT_VALUE
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

    companion object {
        private const val DOWNLOAD_ONLY_ON_WIFI = "download_only_on_wifi"
        private const val TRANSCRIPT_LANGUAGE = "transcript_language"
        private const val PLAYBACK_SPEED = "playback_speed"
        private const val VIDEO_QUALITY = "video_quality"
        private const val DOWNLOAD_TO_SDCARD = "download_to_sdcard"
        private const val SPEED_TEST_KBPS = "speed_test_kbps"
    }
}
