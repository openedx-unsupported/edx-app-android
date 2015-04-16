package org.edx.mobile.util;

import android.content.Context;

import org.edx.mobile.base.MainApplication;


public class ResourceUtil {


    public static String getResourceString(String name) {
        Context context = MainApplication.instance().getApplicationContext();
        return getResourceString(name, context);
    }

    /**
     * get the string resources dynamically
     * @param name
     * @param context
     * @return
     */
    public static String getResourceString(String name, Context context) {
        int nameResourceID = context.getResources().getIdentifier(name, "string", context.getApplicationInfo().packageName);
        if (nameResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + name);
        } else {
            return context.getString(nameResourceID);
        }
    }
}
