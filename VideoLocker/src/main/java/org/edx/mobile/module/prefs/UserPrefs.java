package org.edx.mobile.module.prefs;


import android.content.Context;
import android.os.Environment;

import org.edx.mobile.model.api.ProfileModel;

import java.io.File;

public class UserPrefs {

    private Context context;

    public UserPrefs(Context context) {
        this.context = context;
    }

    /**
     * Returns true if the "download over wifi only" is turned ON, false otherwise.
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
     * Returns user storage directory under /Android/data/ folder for the currently logged in user.
     * This is the folder where all video downloads should be kept.
     * @return
     */
    public File getDownloadFolder() {
        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        ProfileModel profile = pref.getCurrentUserProfile();
        
        File android = new File(Environment.getExternalStorageDirectory(), "Android");
        File downloadsDir = new File(android, "data");
        File packDir = new File(downloadsDir, context.getPackageName());
        File edxDir = new File(packDir, profile.username);
        edxDir.mkdirs();
        
        return edxDir;
    }

    public ProfileModel getProfile() {
        PrefManager pm = new PrefManager(context, PrefManager.Pref.LOGIN);
        return pm.getCurrentUserProfile();
    }
}
