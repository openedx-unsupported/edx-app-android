package org.edx.mobile.util;

import java.io.InputStream;
import java.util.Properties;

import android.content.Context;

public class PropertyUtil {

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
            ex.printStackTrace();
        }
        
        return null;
    }
}
