package org.edx.mobile.module.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.edx.mobile.base.MainApplication
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is a Utility for reading and writing to shared preferences.
 * This class also contains the constants for the preference names and the keys.
 * These constants are defined in inner classes `Pref` and `Key`.
 */
@Singleton
open class PrefBaseManager @Inject constructor(var context: Context, prefName: String) {

    init {
        if (MainApplication.instance() != null) this.context =
            MainApplication.instance().applicationContext
    }

    private val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    protected val gson: Gson = GsonBuilder().create()

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - String
     */
    fun put(key: String, value: String?) {
        editor.putString(key, value).apply()
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - boolean
     */
    fun put(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - long
     */
    fun put(key: String?, value: Long) {
        editor.putLong(key, value).apply()
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - float
     */
    fun put(key: String?, value: Float) {
        editor.putFloat(key, value).apply()
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - int
     */
    fun put(key: String?, value: Int) {
        editor.putInt(key, value).apply()
    }

    /**
     * Returns String value for the given key, null if no value is found.
     *
     * @param key
     * @return String
     */
    fun getString(key: String): String? = sharedPreferences.getString(key, null)

    /**
     * Returns String value for the given key, null if no value is found.
     *
     * @param key
     * @param defaultValue
     * @return String
     */
    fun getString(key: String, defaultValue: String): String =
        sharedPreferences.getString(key, defaultValue) ?: defaultValue

    /**
     * Returns boolean value for the given key, can set default value as well.
     *
     * @param key
     * @param defaultValue
     * @return boolean
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    /**
     * Returns long value for the given key, -1 if no value is found.
     *
     * @param key
     * @param defaultValue
     * @return long
     */
    fun getLong(key: String, defaultValue: Long = -1): Long =
        sharedPreferences.getLong(key, defaultValue)

    /**
     * Returns float value for the given key, defaultValue if no value is found.
     *
     * @param key
     * @param defaultValue
     * @return float
     */
    fun getFloat(key: String, defaultValue: Float = -1f): Float =
        sharedPreferences.getFloat(key, defaultValue)

    /**
     * Returns int value for the given key, defaultValue if no value is found.
     *
     * @param key,default value
     * @return int
     */
    fun getInt(key: String, defaultValue: Int = -1): Int =
        sharedPreferences.getInt(key, defaultValue)

    fun removeKey(key: String) = editor.remove(key).apply()

    fun clear() = editor.clear().apply()

    fun migrateData(oldPrefBaseManager: PrefBaseManager) {
        oldPrefBaseManager.sharedPreferences.all.forEach {
            when (it.value) {
                is Int -> put(it.key, it.value as Int)
                is Long -> put(it.key, it.value as Long)
                is Float -> put(it.key, it.value as Float)
                is Boolean -> put(it.key, it.value as Boolean)
                is String -> put(it.key, it.value as String)
            }
            oldPrefBaseManager.removeKey(it.key)
        }
    }

    /**
     * Contains preference name constants. These must be unique.
     */
    object Pref {
        const val LOGIN = "pref_login"
        const val WIFI = "pref_wifi"
        const val VIDEOS = "pref_videos"
        const val APP_FEATURES = "app_features"
        const val COURSE_CALENDAR_PREF = "course_calendar_pref"
        const val APP_INFO = "pref_app_info"
        const val USER_PREF = "pref_user"
        val all: Array<String>
            get() = arrayOf(
                LOGIN,
                WIFI,
                VIDEOS,
                APP_FEATURES,
                COURSE_CALENDAR_PREF,
                APP_INFO,
                USER_PREF
            )

        @JvmStatic
        val allPreferenceFileNames: Array<String>
            get() {
                val preferencesFilesList = all
                for (i in preferencesFilesList.indices) {
                    preferencesFilesList[i] += ".xml"
                }
                return preferencesFilesList
            }
    }

    object Value {
        /*
         * These values are used in API endpoint
         */
        const val BACKEND_FACEBOOK = "facebook"
        const val BACKEND_GOOGLE = "google-oauth2"
        const val BACKEND_MICROSOFT = "azuread-oauth2"
    }

    companion object {
        const val DEFAULT_VALUE = "NONE"

        /**
         * Clears all the shared preferences that are used in the app.
         *
         * @param exceptions Names of the preferences that need to be skipped while clearing.
         */
        @JvmStatic
        fun nukeSharedPreferences(exceptions: List<String?>) {
            for (prefName in Pref.all) {
                if (exceptions.contains(prefName)) {
                    continue
                }
                MainApplication.application.getSharedPreferences(
                    prefName, Context.MODE_PRIVATE
                ).edit().clear().apply()
            }
        }
    }
}
