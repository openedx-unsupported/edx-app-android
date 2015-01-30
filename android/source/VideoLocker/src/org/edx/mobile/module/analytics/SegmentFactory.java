package org.edx.mobile.module.analytics;

import android.content.Context;

public class SegmentFactory {

    private static ISegment sInstance;

    /**
     * Returns a singleton instance of {@link org.edx.mobile.module.analytics.ISegment}.
     * Use {@link #makeInstance(android.content.Context)} to create an instance.
     * @return
     */
    public static ISegment getInstance() {
        return sInstance;
    }

    /**
     * Creates a singleton instance of {@link org.edx.mobile.module.analytics.ISegment}.
     * If writeKey is not configured, then {@link org.edx.mobile.module.analytics.SegmentTracker}
     * instance is still created, but analytics events are not submitted.
     * @param context
     */
    public static void makeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ISegmentImpl(context);
        }
    }
}
