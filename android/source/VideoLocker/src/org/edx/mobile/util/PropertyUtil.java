package org.edx.mobile.util;

import android.content.Context;
import java.io.InputStream;
import java.util.Properties;
import org.edx.mobile.logger.Logger;

public class PropertyUtil {

    private static final Logger logger = new Logger(PropertyUtil.class.getName());

    public static String getDisplayVersionName(Context context) {
        try {
            Properties props=new Properties();
            InputStream inputStream = context.getAssets().open("config/config.properties");
            props.load(inputStream);
            
            String versionName = props.getProperty("versionName");
            String versionNameSuffix = props.getProperty("versionNameSuffix");
            String buildConfig = props.getProperty("buildConfig");
            if (buildConfig != null && buildConfig.equalsIgnoreCase("prod")) {
                buildConfig = "";
            }
            
            if (buildConfig != null && buildConfig.length() > 1) {
                // init capital for the config name
                buildConfig = Character.toUpperCase(buildConfig.charAt(0)) + buildConfig.substring(1);
            }
            
            return versionName + "." + versionNameSuffix + " " + buildConfig;
        } catch(Exception ex) {
            logger.error(ex);
        }
        return null;
    }
}
