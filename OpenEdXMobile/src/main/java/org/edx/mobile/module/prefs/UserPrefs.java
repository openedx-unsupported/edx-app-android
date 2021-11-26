package org.edx.mobile.module.prefs;


import android.content.Context;

import androidx.annotation.Nullable;

import org.edx.mobile.model.api.ProfileModel;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class UserPrefs {

    private final Context context;

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    public UserPrefs(@ApplicationContext Context context) {
        this.context = context;
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

    public boolean isDownloadToSDCardEnabled() {
        final PrefManager prefManger = new PrefManager(context, PrefManager.Pref.USER_PREF);
        return prefManger.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, false);
    }

    @Nullable
    public ProfileModel getProfile() {
        return loginPrefs.getCurrentUserProfile();
    }
}
