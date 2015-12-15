package org.edx.mobile.logger;

import com.crashlytics.android.Crashlytics;
import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.util.Config;

import java.io.Serializable;

/**
 * Created by shahid on 22/1/15.
 */
public class Logger implements Serializable {

    private String tag;

    @Inject
    Config config;

    private Logger() {}

    public Logger(Class<?> cls) {
        this.tag = cls.getName();
    }

    public Logger(String tag) {
        this.tag = tag;
    }

    /**
     * Prints the stack trace for the given throwable instance for debug build.
     * @param ex
     */
    public void error(Throwable ex) {
        error(ex, false);
    }

    /**
     * Prints the stack trace for the given throwable instance for debug build.
     * Also, submits the crash report (only for release build) to Fabric if submitCrashReport is true.
     * @param ex
     * @param submitCrashReport
     */
    public void error(Throwable ex, boolean submitCrashReport) {
        LogUtil.error(this.tag, "", ex);

        if (submitCrashReport
                && !BuildConfig.DEBUG
                &&  config.getFabricConfig().isEnabled()) {
            Crashlytics.logException(ex);
        }
    }

    public void warn(String log) {
        LogUtil.warn(this.tag, log);
    }

    public void debug(String log) {
        LogUtil.debug(this.tag, log);
    }
}
