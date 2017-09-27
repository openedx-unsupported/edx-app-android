package org.edx.mobile.module.prefs;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
    private final Logger logger = new Logger(getClass().getName());

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

    /**
     * Returns user storage directory under /Android/data/ folder for the currently logged in user
     * or if the sd-card download is enabled the sd-card data location will be used.
     * This is the folder where all video downloads should be kept.
     *
     * @return
     */
    @Nullable
    public File getDownloadDirectory() {
        final PrefManager prefManger = new PrefManager(context, PrefManager.Pref.SD_CARD);
        File downloadDir = null;
        if (prefManger.getBoolean(PrefManager.Key.DOWNLOAD_TO_SDCARD, false)) {
            downloadDir = FileUtil.getRemovableStorageAppDir(context);
        }

        // If no removable storage found, set app internal storage directory as download directory
        if (downloadDir == null) {
            downloadDir = FileUtil.getExternalAppDir(context);
        }
        final ProfileModel profile = getProfile();
        if (downloadDir != null && profile != null) {
            File videosDir = new File(downloadDir, AppConstants.Directories.VIDEOS);
            File usersVidsDir = new File(videosDir, Sha1Util.SHA1(profile.username));
            usersVidsDir.mkdirs();
            try {
                File noMediaFile = new File(usersVidsDir, ".nomedia");
                noMediaFile.createNewFile();
            } catch (IOException ioException) {
                logger.error(ioException);
            }

            return usersVidsDir;
        }
        return null;
    }

    @Nullable
    public ProfileModel getProfile() {
        return loginPrefs.getCurrentUserProfile();
    }
}
