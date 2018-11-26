package org.edx.mobile.module.prefs;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.Sha1Util;

import java.io.File;
import java.io.IOException;

@Singleton
public class UserPrefs {

    private Context context;

    @NonNull
    private final LoginPrefs loginPrefs;

    @Inject
    public UserPrefs(Context context, @NonNull LoginPrefs loginPrefs) {
        this.context = context;
        this.loginPrefs = loginPrefs;
    }

    /**
     * Returns true if the "download over wifi only" is turned ON, false otherwise.
     *
     * @return
     */
    public boolean isDownloadOverWifiOnly() {
        // check if download is only allowed over wifi
        final PrefManager wifiPrefManager = new PrefManager(context,
                PrefManager.Pref.WIFI);
        boolean onlyWifi = wifiPrefManager.getBoolean(
                PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
        return onlyWifi;
    }

    public boolean isDownloadToSDCardEnabled(){
        final PrefManager prefManger = new PrefManager(context, PrefManager.Pref.USER_PREF);
        return prefManger.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, true);
    }

    @Nullable
    public ProfileModel getProfile() {
        return loginPrefs.getCurrentUserProfile();
    }
}
