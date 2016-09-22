package org.edx.mobile.logger;

import android.util.Log;

import org.edx.mobile.BuildConfig;

class LogUtil {
    /**
     * Prints given tag and text as debug log, only in debug builds.
     * @param tag - Tag to be set for logs
     * @param text - Log message
     */
    public static void debug(String tag, String text) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, text);
        }
    }

    /**
     * Prints given tag and text as warning log, only in debug builds.
     * @param tag - Tag to be set for logs
     * @param text - Log message
     */
    public static void warn(String tag, String text) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, text);
        }
    }
    
    /**
     * Prints given tag and text as error log, only in debug builds.
     * @param tag - Tag to be set for logs
     * @param text - Log message
     */
    public static void error(String tag, String text) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, text);
        }
    }

    /**
     * Prints given tag, message, and exception as error log, only in debug builds.
     * @param tag - Tag to be set for logs
     * @param msg - Log message
     * @param ex  - Exception to be logged
     */
    public static void error(String tag, String msg, Throwable ex) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, ex);
        }
    }

    /**
     * Prints given tag and exception as error log, only in debug builds.
     * @param tag - Tag to be set for logs
     * @param ex  - Exception to be logged
     */
    public static void error(String tag, Throwable ex) {
        error(tag,"",ex);
    }
}
