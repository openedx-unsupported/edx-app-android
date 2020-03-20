package org.edx.mobile.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.db.DbStructure;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.CourseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import roboguice.RoboGuice;

/**
 * Utility class dealing with the security of a user's personal information data.
 */
public class SecurityUtil {
    private static final Logger logger = new Logger(SecurityUtil.class);

    // Make this class non-instantiable
    private SecurityUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Clears the app's data directory, external storage directory and shared preferences,
     * with the exceptions of downloaded videos and videos database.
     *
     * @param context The current context.
     */
    public static void clearUserData(@NonNull Context context) {
        // Add all preference files and db in exceptions list
        final ArrayList<String> exceptionsList = new ArrayList<>();
        Collections.addAll(exceptionsList, PrefManager.Pref.getAllPreferenceFileNames());
        exceptionsList.add(DbStructure.NAME);

        // Clear the data directory
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            File dataDir = new File(packageInfo.applicationInfo.dataDir);
            File[] filesList = dataDir.listFiles();
            if (filesList != null) {
                for (final File child : filesList) {
                    FileUtil.deleteRecursive(child, exceptionsList);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen as we've given our app's package name to getPackageInfo function.
            logger.error(e);
        }

        // Now clear the App's external storage directory
        File externalAppDir = FileUtil.getExternalAppDir(context);
        File[] filesList = externalAppDir.listFiles();
        if (filesList != null) {
            for (final File child : filesList) {
                FileUtil.deleteRecursive(child, Collections.singletonList(AppConstants.Directories.VIDEOS));
            }
        }

        // Now clear all the shared preferences except app related preferences
        PrefManager.nukeSharedPreferences(Collections.singletonList(PrefManager.Pref.APP_INFO));

        // Clear app level caching of all courses
        final CourseManager courseManager = RoboGuice.getInjector(context).getInstance(CourseManager.class);
        courseManager.clearAllAppLevelCache();
    }
}
