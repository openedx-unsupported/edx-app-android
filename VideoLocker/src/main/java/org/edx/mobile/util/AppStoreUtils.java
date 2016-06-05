package org.edx.mobile.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.edx.mobile.BuildConfig;

/**
 * Utility class for interacting with an app store, or the
 * Play Store specifically.
 */
public final class AppStoreUtils {
    // Make this class non-instantiable
    private AppStoreUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Open an app store to display the app.
     *
     * @param context A Context for starting the new Activity
     */
    public static void openAppInAppStore(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" +
                            BuildConfig.APPLICATION_ID));
            context.startActivity(intent);
        }
    }
}
