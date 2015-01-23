package org.edx.mobile.logger;

/**
 * Created by shahid on 22/1/15.
 */
public class OEXLogger {

    private String tag;

    private OEXLogger() {}

    public OEXLogger(Class<?> cls) {
        this.tag = cls.getName();
    }

    public OEXLogger(String tag) {
        this.tag = tag;
    }

    public void error(Throwable ex) {
        LogUtil.error(this.tag, "", ex);
    }

    public void warn(String log) {
        LogUtil.warn(this.tag, log);
    }

    public void debug(String log) {
        LogUtil.debug(this.tag, log);
    }
}
