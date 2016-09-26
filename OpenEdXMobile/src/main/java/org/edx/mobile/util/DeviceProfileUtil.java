package org.edx.mobile.util;

import android.annotation.SuppressLint;
import android.os.Build;

/**
 * This class is used for Device Profile
 */
@SuppressLint("DefaultLocale")
public class DeviceProfileUtil {

    public static String getManufacturerName(){
        return Build.MANUFACTURER.toUpperCase();
    }
    
    public static String getModelName(){
        return Build.MODEL.toUpperCase();
    }
    
    public static String getOSType(){
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceProfileInfo(){
        String manufacturerName = getManufacturerName();
        String modelName = getModelName();
        String osType = getOSType();
        return manufacturerName +  ":" + modelName + ":" + osType;
    }

    public static boolean isSamsungGalaxyS3(){
        String manufacturerName = getManufacturerName();
        String modelName = getModelName();
        return "SAMSUNG".equals(manufacturerName) && "SAMSUNG-SGH-I747".equals(modelName);
    }

}
