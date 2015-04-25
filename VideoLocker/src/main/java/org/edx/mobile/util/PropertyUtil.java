package org.edx.mobile.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.InputStream;
import java.util.Properties;
import org.edx.mobile.logger.Logger;

public class PropertyUtil {

    private static final Logger logger = new Logger(PropertyUtil.class.getName());

    public static String getManifestVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionName;
        } catch(Exception ex) {
            logger.error(ex);
        }
        return null;
    }

    /**
     * check version number is a better approach comparing with Version Name.
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getManifestVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return -1;
        }
    }
}
