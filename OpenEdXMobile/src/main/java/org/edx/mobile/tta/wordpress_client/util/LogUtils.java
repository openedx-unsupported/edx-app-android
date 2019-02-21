/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edx.mobile.tta.wordpress_client.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogUtils {

    private static final boolean DISK_LOGGING_ENABLE = false;

    static String className;
    static String methodName;

    private LogUtils() {
    }

    public static boolean isDebugBuild() {
        return true;
        //return BuildConfig.DEBUG;
    }

    private static String createLogStr(String msg) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(className);
        buffer.append(".");
        buffer.append(methodName);
        buffer.append("] ");
        buffer.append(msg);

        return buffer.toString();
    }

    private static void setTagInfo(StackTraceElement element) {
        className = element.getFileName().replace(".java", "");
        methodName = element.getMethodName();
    }

    private static void writeToFile(String msg) {
        try {
            File output = new File(Environment.getExternalStorageDirectory(), "independent.log");
            FileWriter writer = new FileWriter(output, true);

            writer.write(msg);
            writer.write(System.getProperty("line.separator"));
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void d(String message) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.d(className, createLogStr(message));
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void d(String message, Throwable cause) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.d(className, createLogStr(message), cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void d(String tag, String message) {
        if (isDebugBuild()) {
            Log.d(tag, message);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void d(String tag, String message, Throwable cause) {
        if (isDebugBuild()) {
            Log.d(tag, message, cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void v(String message) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.v(className, createLogStr(message));
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void v(String message, Throwable cause) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.v(className, createLogStr(message), cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void v(String tag, String message) {
        if (isDebugBuild()) {
            Log.v(tag, message);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void v(String tag, String message, Throwable cause) {
        if (isDebugBuild()) {
            Log.v(tag, message, cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void i(String message) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.i(className, createLogStr(message));
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void i(String message, Throwable cause) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.i(className, createLogStr(message), cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void i(String tag, String message) {
        if (isDebugBuild()) {
            Log.i(tag, message);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void i(String tag, String message, Throwable cause) {
        if (isDebugBuild()) {
            Log.i(tag, message, cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void w(String message) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.w(className, createLogStr(message));
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void w(String message, Throwable cause) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.w(className, createLogStr(message), cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void w(String tag, String message) {
        if (isDebugBuild()) {
            Log.w(tag, message);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void w(String tag, String message, Throwable cause) {
        if (isDebugBuild()) {
            Log.w(tag, message, cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void e(String message) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.e(className, createLogStr(message));
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void e(String message, Throwable cause) {
        if (isDebugBuild()) {
            setTagInfo(new Throwable().getStackTrace()[1]);
            Log.e(className, createLogStr(message), cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void e(String tag, String message) {
        if (isDebugBuild()) {
            Log.e(tag, message);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }

    public static void e(String tag, String message, Throwable cause) {
        if (isDebugBuild()) {
            Log.e(tag, message, cause);
        }

        if (DISK_LOGGING_ENABLE) {
            writeToFile(message);
        }
    }
}
