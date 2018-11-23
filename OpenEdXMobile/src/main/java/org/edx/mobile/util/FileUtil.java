package org.edx.mobile.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.UserPrefs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class FileUtil {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    // Make this class non-instantiable
    private FileUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Utility method to determine if any removable storage (such as an SD card) is available.
     *
     * @param context The current context
     * @return True if there is removable storage available on the device.
     */
    public static boolean isRemovableStorageAvailable(@NonNull Context context) {
        return getRemovableStorageAppDir(context) != null;
    }

    @Nullable
    public static File getRemovableStorageAppDir(@NonNull Context context) {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            File[] fileList = context.getExternalFilesDirs(null);
            for (File extFile : fileList) {
                if (extFile != null && Environment.isExternalStorageRemovable(extFile)) {
                    return extFile;
                }
            }
        }
        return null;
    }

    /**
     * Utility function for getting the app's external storage directory.
     *
     * @param context The current context.
     * @return The app's external storage directory.
     */
    @Nullable
    public static File getExternalAppDir(@NonNull Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);
        return (externalFilesDir != null ? externalFilesDir.getParentFile() : null);
    }

    /**
     * Returns user storage directory under /Android/data/ folder for the currently logged in user
     * or if the sd-card download is enabled the sd-card data location will be used.
     * This is the folder where all video downloads should be kept.
     *
     * @param context
     * @param environment
     * @param logger
     * @return
     */
    @Nullable
    public static File getDownloadDirectory(Context context, IEdxEnvironment environment, Logger logger) {
        File downloadDir;
        UserPrefs userPref = environment.getUserPrefs();
        if (environment.getConfig().isSDCardDownloadEnabled() && userPref.isSDCardDownloadEnabled()
                && FileUtil.isRemovableStorageAvailable(context)) {
            downloadDir = FileUtil.getRemovableStorageAppDir(context);
        } else {
            // If no removable storage found, set app internal storage directory
            // as download directory
            downloadDir = FileUtil.getExternalAppDir(context);
        }

        final ProfileModel profile = userPref.getProfile();
        if (downloadDir != null && profile != null) {
            File videosDir = new File(downloadDir, AppConstants.Directories.VIDEOS);
            File usersVideosDir = new File(videosDir, Sha1Util.SHA1(profile.username));
            usersVideosDir.mkdirs();
            try {
                File noMediaFile = new File(usersVideosDir, ".nomedia");
                noMediaFile.createNewFile();
            } catch (IOException ioException) {
                logger.error(ioException);
            }

            return usersVideosDir;
        }
        return null;
    }

    /**
     * Returns the text of a file as a String object
     *
     * @param context  The current context
     * @param fileName The name of the file to load from assets folder
     * @return The text content of the file
     */
    public static String loadTextFileFromAssets(Context context, String fileName)
            throws IOException {
        return getStringFromInputStream(context.getAssets().open(fileName));
    }

    /**
     * Returns the text of a file as a String object
     *
     * @param context The current context
     * @param fileId  The resource id of a file to load
     * @return The text content of the file
     */
    public static String loadTextFileFromResources(@NonNull Context context,
                                                   @RawRes int fileId) throws IOException {
        return getStringFromInputStream(context.getResources().openRawResource(fileId));
    }

    private static String getStringFromInputStream(@NonNull InputStream inputStream)
            throws IOException {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            try {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                for (int n; (n = inputStream.read(buffer)) >= 0; ) {
                    outputStream.write(buffer, 0, n);
                }
                return outputStream.toString();
            } finally {
                outputStream.close();
            }
        } finally {
            inputStream.close();
        }
    }

    /**
     * Deletes a file or directory and all its content recursively.
     *
     * @param fileOrDirectory The file or directory that needs to be deleted.
     */
    public static void deleteRecursive(@NonNull File fileOrDirectory) {
        deleteRecursive(fileOrDirectory, Collections.EMPTY_LIST);
    }

    /**
     * Deletes a file or directory and all its content recursively.
     *
     * @param fileOrDirectory The file or directory that needs to be deleted.
     * @param exceptions      Names of the files or directories that need to be skipped while deletion.
     */
    public static void deleteRecursive(@NonNull File fileOrDirectory,
                                       @NonNull List<String> exceptions) {
        if (exceptions.contains(fileOrDirectory.getName())) return;

        if (fileOrDirectory.isDirectory()) {
            File[] filesList = fileOrDirectory.listFiles();
            if (filesList != null) {
                for (File child : filesList) {
                    deleteRecursive(child, exceptions);
                }
            }
        }

        // Don't break the recursion upon encountering an error
        // noinspection ResultOfMethodCallIgnored
        fileOrDirectory.delete();
    }
}
