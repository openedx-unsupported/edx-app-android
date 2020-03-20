package org.edx.mobile.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;

import java.util.List;

/**
 * Utility class for app store related interactions in the app.
 */
public final class AppStoreUtils {
    // Make this class non-instantiable
    private AppStoreUtils() {
        throw new UnsupportedOperationException();
    }

    @Inject
    private static Config config;

    /**
     * @param context A Context to query the applications info.
     *
     * @return Whether there are any apps registered to handle the update URIs.
     */
    public static boolean canUpdate(@NonNull final Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        final PackageManager packageManager = context.getPackageManager();
        for (final Uri uri : config.getAppStoreUris()) {
            intent.setData(uri);
            if (intent.resolveActivity(packageManager) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Open a native app or a website on a web browser.
     *
     * @param context A Context for starting the new Activity.
     */
    public static void openAppInAppStore(@NonNull final Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        /* The app store would be expected to be opened in a
         * separate task. The Play Store is already
         * configured to work this way by default.
         */
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final List<Uri> uris = config.getAppStoreUris();
        if (uris.isEmpty()) return;

        /* Try to resolve the app store that was initially used to
         * install the install the app, if it was installed through
         * one, and it's resolved through one of the registered URIs.
         */
        final PackageManager packageManager = context.getPackageManager();
        final String installerPackageName = packageManager
                .getInstallerPackageName(BuildConfig.APPLICATION_ID);
        if (installerPackageName != null) {
            for (final Uri uri : uris) {
                intent.setData(uri);
                final List<ResolveInfo> resolveInfoList =
                        packageManager.queryIntentActivities(intent, 0);
                for (final ResolveInfo resolveInfo : resolveInfoList) {
                    final ActivityInfo activityInfo = resolveInfo.activityInfo;
                    final String packageName = activityInfo.applicationInfo.packageName;
                    if (packageName.equals(installerPackageName)) {
                        /* The app store responsible for initially installing the app
                         * has been found; try to start it's Activity explicitly by
                         * setting it's component data, in order to avoid contention by
                         * other applications that are also registered to handle the
                         * same URI.
                         */
                        intent.setClassName(packageName, activityInfo.name);
                        try {
                            context.startActivity(intent);
                            return;
                        } catch (ActivityNotFoundException e) {
                            /* The application was uninstalled or updated before the
                             * Activity was started. Remove the component information,
                             * and continue iterating through the handling Activities.
                             */
                            intent.setComponent(null);
                        }
                    }
                }
            }
            /* None of the registered URIs resolve to the app store that was initially
             * used to install the app. It has either been uninstalled, or it's not
             * officially supported by the developer. In either case we can disregard
             * it, and fall back to resolving to any of the registered URIs in order of
             * precedence.
             */
        }

        // Iterate through all the registered URIs, and start
        // an Activity from the first one that can be resolved.
        for (final Uri uri : uris) {
            intent.setData(uri);
            try {
                context.startActivity(intent);
                return;
            } catch (ActivityNotFoundException e) {
                // Continue iterating until an Activity
                // is resolved against one of the URIs.
            }
        }

        // No Activity was resolved against any of the registered
        // URIS. Show a toast message to that effect.
        Toast.makeText(context, R.string.app_version_upgrade_app_store_unavailable,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Generic click listener that opens an app store to display the app. This is created as a
     * convenience, because this utility seems to be mostly invoked from a click listener.
     */
    public static final View.OnClickListener OPEN_APP_IN_APP_STORE_CLICK_LISTENER =
            new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    openAppInAppStore(v.getContext());
                }
            };
}
