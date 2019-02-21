package org.edx.mobile.tta.wordpress_client.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.edx.mobile.R;


/**
 * Created by jlo on 2015/05/18.
 */
public class WordpressPreferenceHelper extends BasePreferenceHelper {

    private static final String APP_PREFS = "wordpress_preferences";

    private static final String PREF_INITIAL_SETUP_DONE = "pref.initial_setup_done";

    private static final int PREF_USER_WP_ID = R.string.pref_id_wordpress_id;
    private static final int PREF_USER_WP_USERNAME = R.string.pref_id_wordpress_username;
    private static final int PREF_USER_WP_PASSWORD = R.string.pref_id_wordpress_password;

    private static WordpressPreferenceHelper sInstance = null;

    public static WordpressPreferenceHelper with(Context context) {
        if (sInstance == null) {
            sInstance = new WordpressPreferenceHelper(context);
        }

        return sInstance;
    }

    private WordpressPreferenceHelper(Context context) {
        if (context == null) {
            throw new IllegalStateException("Context can not be null!");
        }
        mContext = context;
    }

    @Override
    protected String getAppPreferenceName() {
        return APP_PREFS;
    }

    /* APP PREFERENCES */

    public boolean isInitialSetupDone() {
        return getBooleanPref(PREF_INITIAL_SETUP_DONE);
    }

    public void setInitialSetupDone(boolean value) {
        putBooleanPref(PREF_INITIAL_SETUP_DONE, value);
    }

    public void resetUserState() {
        SharedPreferences preferences = getPreferences();
        preferences.edit().clear().commit();
    }

    public WordpressPreferenceHelper setWordPressUserId(long id) {
        String pref = mContext.getString(PREF_USER_WP_ID);
        putLongPref(pref, id);
        return this;
    }

    public long getWordPressUserId() {
        String pref = mContext.getString(PREF_USER_WP_ID);
        return getLongPref(pref);
    }

    public boolean hasWordPressUserId() {
        return getWordPressUserId() != -1;
    }

    public WordpressPreferenceHelper setWordPressUsername(String username) {
        String pref = mContext.getString(PREF_USER_WP_USERNAME);
        putStringPref(pref, username);
        return this;
    }

    public String getWordPressUsername() {
        String pref = mContext.getString(PREF_USER_WP_USERNAME);
        return getStringPref(pref);
    }

    public WordpressPreferenceHelper setWordPressUserPassword(String password) {
        String pref = mContext.getString(PREF_USER_WP_PASSWORD);
        putStringPref(pref, password);
        return this;
    }

    public String getWordPressUserPassword() {
        String pref = mContext.getString(PREF_USER_WP_PASSWORD);
        return getStringPref(pref);
    }
}
