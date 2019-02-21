package org.edx.mobile.tta.wordpress_client.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.edx.mobile.R;


/**
 * @author Arjun Singh
 *         Created on 2016/02/26.
 */
public class LoginAccountHelper extends BasePreferenceHelper {

    private static final String APP_PREFS = "account_preferences";

    /* USER CONSTANTS */
    private static final int PREF_USER_HAS_PROFILE = R.string.pref_id_has_profile;
    private static final int PREF_USER_ID = R.string.pref_id_username;
    private static final int PREF_USER_EMAIL = R.string.pref_id_email;
    private static final int PREF_USER_NAME = R.string.pref_id_name;
    private static final int PREF_USER_FIRST_NAME = R.string.pref_id_first_name;
    private static final int PREF_USER_LAST_NAME = R.string.pref_id_last_name;
    private static final int PREF_USER_PROFILE_PIC = R.string.pref_id_profile_pic;
    private static final int PREF_USER_ACC_TYPE = R.string.pref_id_acc_type;

    public static final String ACC_TYPE_FACEBOOK = "facebook";
    public static final String ACC_TYPE_GOOGLE = "google";

    private static LoginAccountHelper sInstance = null;

    public static LoginAccountHelper with(Context context) {
        if (sInstance == null) {
            sInstance = new LoginAccountHelper(context);
        }

        return sInstance;
    }

    private LoginAccountHelper(Context context) {
        if (context == null) {
            throw new IllegalStateException("Context can not be null!");
        }
        mContext = context;
    }

    @Override
    protected String getAppPreferenceName() {
        return APP_PREFS;
    }

    public void resetUserState() {
        SharedPreferences preferences = getPreferences();
        preferences.edit().clear().commit();
    }

    // USER PREFERENCES

    public LoginAccountHelper setUserHasProfile(boolean hasProfile) {
        String pref = mContext.getString(PREF_USER_HAS_PROFILE);
        putBooleanPref(pref, hasProfile);
        return this;
    }

    public boolean getUserHasProfile() {
        String pref = mContext.getString(PREF_USER_HAS_PROFILE);
        return getBooleanPref(pref);
    }

    public LoginAccountHelper setUserId(String id) {
        String pref = mContext.getString(PREF_USER_ID);
        putStringPref(pref, id);
        return this;
    }

    public String getUserId() {
        String pref = mContext.getString(PREF_USER_ID);
        return getStringPref(pref);
    }

    public LoginAccountHelper setUserEmail(String email) {
        String pref = mContext.getString(PREF_USER_EMAIL);
        putStringPref(pref, email);
        return this;
    }

    public String getUserEmail() {
        String pref = mContext.getString(PREF_USER_EMAIL);
        return getStringPref(pref);
    }

    public LoginAccountHelper setUserName(String name) {
        String pref = mContext.getString(PREF_USER_NAME);
        putStringPref(pref, name);
        return this;
    }

    public String getUserName() {
        String pref = mContext.getString(PREF_USER_NAME);
        return getStringPref(pref);
    }

    public LoginAccountHelper setUserFirstName(String firstName) {
        String pref = mContext.getString(PREF_USER_FIRST_NAME);
        putStringPref(pref, firstName);
        return this;
    }

    public String getUserFirstName() {
        String pref = mContext.getString(PREF_USER_FIRST_NAME);
        return getStringPref(pref);
    }

    public LoginAccountHelper setUserLastName(String lastName) {
        String pref = mContext.getString(PREF_USER_LAST_NAME);
        putStringPref(pref, lastName);
        return this;
    }

    public String getUserLastName() {
        String pref = mContext.getString(PREF_USER_LAST_NAME);
        return getStringPref(pref);
    }

    public LoginAccountHelper setUserProfilePic(String pic) {
        String pref = mContext.getString(PREF_USER_PROFILE_PIC);
        putStringPref(pref, pic);
        return this;
    }

    public String getUserProfilePic() {
        String pref = mContext.getString(PREF_USER_PROFILE_PIC);
        return getStringPref(pref);
    }

    public LoginAccountHelper setUserAccType(String type) {
        String pref = mContext.getString(PREF_USER_ACC_TYPE);
        putStringPref(pref, type);
        return this;
    }

    public String getUserAccType() {
        String pref = mContext.getString(PREF_USER_ACC_TYPE);
        return getStringPref(pref);
    }
}
