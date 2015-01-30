package org.edx.mobile.util;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import org.edx.mobile.logger.Logger;

public class DeviceSettingUtil {
    private static final Logger logger = new Logger(DeviceSettingUtil.class.getName());

    /**
     * Returns true if device screen rotation is turned ON, false otherwise.
     * @param context
     * @return
     */
    public static boolean isDeviceRotationON(Context context) {
        try {
            if(context==null){
                //Context is null
                return true;
            }
            int str = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION);
            if (str == 1) {
                // rotation is Unlocked
                return true;
            } else {
                // rotation is Locked
                return false;
            }
        } catch (SettingNotFoundException e) {
            logger.error(e);
        } catch (Exception e){
            logger.error(e);
        }
        // default ON
        return true;
    }
}
