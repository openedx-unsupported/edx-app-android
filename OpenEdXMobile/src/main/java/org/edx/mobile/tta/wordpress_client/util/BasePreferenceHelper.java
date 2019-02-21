package org.edx.mobile.tta.wordpress_client.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Arjun Singh
 *         Created on 2016/01/04.
 */
public abstract class BasePreferenceHelper {

    private static final String DEFAULT_STRING = null;
    private static final long DEFAULT_LONG = -1L;
    private static final int DEFAULT_INT = -1;
    private static final float DEFAULT_FLOAT = -1F;
    private static final boolean DEFAULT_BOOLEAN = false;

    protected Context mContext;

    /* APP SPECIFIC METHODS */

    public SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(getAppPreferenceName(), Context.MODE_PRIVATE);
    }

    protected abstract String getAppPreferenceName();

    /* INTERNAL METHODS */

    protected void putStringPref(String key, String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    protected void putLongPref(String key, long value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(key, value);
        editor.apply();
    }

    protected void putBooleanPref(String key, boolean value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    protected void putIntegerPref(String key, int value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(key, value);
        editor.apply();
    }

    protected void putFloatPrefs(String key, float value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    protected String getStringPref(String key, String defaultValue) {
        return getPreferences().getString(key, defaultValue);
    }

    protected String getStringPref(String key) {
        return getStringPref(key, DEFAULT_STRING);
    }

    protected long getLongPref(String key, long defaultValue) {
        return getPreferences().getLong(key, defaultValue);
    }

    protected long getLongPref(String key) {
        return getLongPref(key, DEFAULT_LONG);
    }

    protected int getIntegerPref(String key, int defaultValue) {
        return getPreferences().getInt(key, defaultValue);
    }

    protected int getIntegerPref(String key) {
        return getIntegerPref(key, DEFAULT_INT);
    }

    protected float getFloatPref(String key, float defaultValue) {
        return getPreferences().getFloat(key, defaultValue);
    }

    protected float getFloatPref(String key) {
        return getFloatPref(key, DEFAULT_FLOAT);
    }

    protected boolean getBooleanPref(String key, boolean defaultValue) {
        return getPreferences().getBoolean(key, defaultValue);
    }

    protected boolean getBooleanPref(String key) {
        return getBooleanPref(key, DEFAULT_BOOLEAN);
    }

    protected void removePref(String key) {
        getPreferences().edit().remove(key).commit();
    }
}
