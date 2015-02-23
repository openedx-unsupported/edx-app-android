package org.edx.mobile.module.analytics;

import android.content.Context;

import org.edx.mobile.util.Config;

public class SegmentFactory {

    private static ISegment sInstance;

    /**
     * Returns a singleton instance of {@link org.edx.mobile.module.analytics.ISegment}.
     * Use {@link #makeInstance(android.content.Context)} to create an instance.
     * Returns an instance of {@link org.edx.mobile.module.analytics.ISegmentEmptyImpl} if
     * {@link #makeInstance(android.content.Context)} was never called before.
     * @return
     */
    public static ISegment getInstance() {
        if (sInstance == null) {
            sInstance = new ISegmentEmptyImpl();
        }
        return sInstance;
    }

    /**
     * Creates a singleton instance of {@link org.edx.mobile.module.analytics.ISegment}
     * if not already created.
     * If configuration does not allow third party traffic, then this method makes
     * an instance of {@link org.edx.mobile.module.analytics.ISegmentEmptyImpl} class
     * which does not capture any data.
     * @param context
     */
    public static void makeInstance(Context context) {
        if (sInstance == null) {
            if (Config.getInstance().getSegmentConfig().isEnabled()) {
                sInstance = new ISegmentImpl(context);
            }
            else {
                sInstance = new ISegmentEmptyImpl();
            }
        }
    }
}
