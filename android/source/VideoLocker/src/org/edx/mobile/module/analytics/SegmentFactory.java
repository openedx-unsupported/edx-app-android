package org.edx.mobile.module.analytics;

import android.content.Context;

public class SegmentFactory {

    private static ISegment instance;
    
    public static ISegment getInstance(Context context, SegmentTracker tracker) {
        if (instance == null) {
            instance = new ISegmentImpl(context, tracker);
        }
        
        return instance;
    }
}
