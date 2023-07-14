package org.edx.mobile.module.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.edx.mobile.base.MainApplication

/**
 * This class serves as the base for all preferences, providing a framework for managing shared
 * preferences efficiently. It includes constants for preference names defined in the inner class
 * Pref. Its purpose is to simplify the reading and writing of data to shared preferences.
 */
abstract class PrefBaseManager constructor(
    var context: Context,
    prefName: String
) {

    private val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    protected val gson: Gson = GsonBuilder().create()

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - String?
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
    fun put(key: String, value: Long) {
        editor.putLong(key, value).apply()
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - float
     */
    fun put(key: String, value: Float) {
        editor.putFloat(key, value).apply()
    }

    /**
     * Puts given key-value pair to the Shared Preferences.
     *
     * @param key
     * @param value - int
     */
    fun put(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    /**
     * Returns String value for the given key, null if no value is found.
     *
     * @param key
     * @return String or null
     */
    fun getString(key: String): String? = sharedPreferences.getString(key, null)

    /**
     * Returns String value for the given key, defaultValue if no value is found.
     *
     * @param key
     * @param defaultValue
     * @return String or defaultValue if no value is found
     */
    fun getString(key: String, defaultValue: String): String =
        sharedPreferences.getString(key, defaultValue) ?: defaultValue

    /**
     * Returns boolean value for the given key, defaultValue if no value is found.
     *
     * @param key
     * @param defaultValue
     * @return boolean
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    /**
     * Returns long value for the given key, defaultValue if no value is found.
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
     * @param key
     * @param defaultValue
     * @return int
     */
    fun getInt(key: String, defaultValue: Int = -1): Int =
        sharedPreferences.getInt(key, defaultValue)

    /**
     * Remove the key and its associated value.
     *
     * @param key
     */
    fun removeKey(key: String) = editor.remove(key).apply()

    /**
     * Remove all key value pairs
     */
    fun clear() = editor.clear().apply()

    /**
     * Migrates data from the old preference manager to the new preference manager and remove the old preferences
     *
     * @param oldPrefBaseManager The old preference manager to migrate data from.
     */
    fun migrateData(oldPrefBaseManager: PrefBaseManager) {
        oldPrefBaseManager.sharedPreferences.all.forEach { (key, value) ->
            when (value) {
                is Int -> put(key, value)
                is Long -> put(key, value)
                is Float -> put(key, value)
                is Boolean -> put(key, value)
                is String -> put(key, value)
            }
            oldPrefBaseManager.removeKey(key)
        }
    }

    companion object {

        const val FEATURES = "app_features"
        const val INFO = "pref_app_info"
        const val LOGIN = "pref_login"
        const val USER = "pref_user"
        const val WIFI = "pref_wifi"
        const val VIDEOS = "pref_videos"
        const val COURSE_CALENDAR_PREF = "course_calendar_pref"

        private val preferencesList = listOf(
            FEATURES,
            INFO,
            LOGIN,
            USER,
            WIFI,
            VIDEOS,
            COURSE_CALENDAR_PREF
        )

        val allPreferenceFileNames = preferencesList.map { "$it.xml" }.toTypedArray()

        /**
         * Clears all the shared preferences that are used in the app.
         *
         * @param exceptions Names of the preferences that need to be skipped while clearing.
         */
        @JvmStatic
        fun nukeSharedPreferences(exceptions: List<String?>) {
            preferencesList
                .filterNot { exceptions.contains(it) }
                .forEach { pref ->
                    MainApplication.application.getSharedPreferences(
                        pref, Context.MODE_PRIVATE
                    ).edit().clear().apply()
                }
        }
    }
}
