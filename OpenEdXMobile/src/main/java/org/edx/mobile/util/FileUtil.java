package org.edx.mobile.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

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
