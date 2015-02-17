package org.edx.mobile.module.analytics;

import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

/**
 * Created by rohan on 2/8/15.
 */
public interface ISegmentTracker {

    void resetIdentifyUser();

    /**
     * Calls track method of Analytics.
     * @param event
     * @param props
     */
    void track(String event, Properties props);

    /**
     * Calls screen method of Analytics.
     * @param category
     * @param name
     */
    void screen(String category, String name, Properties properties);

    /**
     * Calls identify method of Analytics.
     * @param id
     * @param traits
     * @param options
     */
    void identify(String id, Traits traits, Options options);
}
