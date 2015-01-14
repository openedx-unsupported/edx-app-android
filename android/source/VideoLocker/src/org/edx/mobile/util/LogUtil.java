package org.edx.mobile.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;


public class LogUtil {

    public static boolean ISDEBUG = true;

    /**
     * Initializes <code>LogUtil.ISDEBUG</code> 
     * 
     * @param context
     */
    public static void init(Context context) {
        boolean isDebuggable =  ( 0 != ( context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        LogUtil.ISDEBUG = isDebuggable;
    }
    
    /**
     * Prints given tag and text as debug log, only if in debug mode.
     * @param tag
     * @param text
     */
    public static void log(String tag, String text) {
        if (LogUtil.ISDEBUG) {
            Log.d(tag, text);
        }
    }
    
    /**
     * Prints given tag and text as error log, only if in debug mode.
     * @param tag
     * @param text
     */
    public static void error(String tag, String text) {
        if (LogUtil.ISDEBUG) {
            Log.e(tag, text);
        }
    }
}
