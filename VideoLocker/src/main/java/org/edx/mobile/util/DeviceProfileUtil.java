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
}
