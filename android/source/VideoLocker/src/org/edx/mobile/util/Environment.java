package org.edx.mobile.util;

import android.content.Context;

/**
 * Created by aleffert on 1/8/15.
 */
public class Environment {
    private static Environment sInstance;

    private Config mConfig;

    public static void makeInstance(Context context) {
        sInstance = new Environment(context);
    }

    public static Environment getInstance() {
        return sInstance;
    }

    private Environment(Context context) {
        mConfig = new Config(context);
    }

    public Config getConfig() {
        return mConfig;
    }
}
