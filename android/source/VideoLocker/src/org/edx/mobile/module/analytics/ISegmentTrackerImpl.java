package org.edx.mobile.module.analytics;

import android.content.Context;

import com.segment.analytics.Analytics;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;

class ISegmentTrackerImpl implements ISegmentTracker {

    /* Singleton instance of Analytics */
    private Analytics analytics;
    private final Logger logger = new Logger(getClass().getName());

    public ISegmentTrackerImpl(Context context) {
        try {
            String writeKey = Config.getInstance().getSegmentIOWriteKey();

            if (writeKey != null) {
                logger.debug("SegmentTracker created with write key: " + writeKey);

                String debugging = context.getString(R.string.analytics_debug);
                int queueSize = context.getResources().getInteger(R.integer.analytics_queue_size);

                // Now Analytics.with will return the custom instance
                analytics = new Analytics.Builder(context, writeKey)
                        .debugging(Boolean.parseBoolean(debugging))
                        .queueSize(queueSize)
                        .build();
            } else {
                logger.warn("writeKey is null, Segment analytics will not work.");
            }
        } catch(RuntimeException ex) {
            logger.error(ex);
        } catch(Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * This function is used to reset the user/
     * flush all remaining events for particular user
     */
    @Override
    public void resetIdentifyUser() {
        try {
            if (analytics != null) {
                analytics.flush();
            }
        } catch(Exception e) {
            logger.error(e);
        }
    }

    /**
     * Calls track method of Analytics.
     * @param event
     * @param props
     */
    @Override
    public void track(String event, Properties props) {
        try {
            if (analytics != null) {
                analytics.track(event, props);
            }
        } catch(Exception e) {
            logger.error(e);
        }
    }

    /**
     * Calls screen method of Analytics.
     * @param category
     * @param name
     */
    @Override
    public  void screen(String category, String name, Properties properties) {
        try {
            if (analytics != null) {
                analytics.screen(category, name, properties);
            }
        } catch(Exception e) {
            logger.error(e);
        }
    }

    /**
     * Calls identify method of Analytics.
     * @param id
     * @param traits
     * @param options
     */
    @Override
    public void identify(String id, Traits traits, Options options) {
        try {
            if (analytics != null) {
                analytics.identify(id, traits, options);
            }
        } catch(Exception e) {
            logger.error(e);
        }
    }
}
