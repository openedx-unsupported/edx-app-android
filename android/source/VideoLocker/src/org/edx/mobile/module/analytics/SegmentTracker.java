package org.edx.mobile.module.analytics;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;

import android.content.Context;
import com.segment.analytics.Analytics;
import com.segment.analytics.AnalyticsContext;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

public class SegmentTracker {

    /* Singleton instance of Analytics */
    private Analytics analytics;
    private final Logger logger = new Logger(getClass().getName());
    
    public SegmentTracker(Context context) {
        try {
            Config config = Config.getInstance();

            String writeKey = config.getSegmentIOWriteKey();
            boolean debugging = config.getSegmentDebugOn();
            int queueSize = config.getSegmentQueueSize();
            
            // Must be called before any calls to Analytics.with(context)
            // Now Analytics.with will return the custom instance

            if (writeKey != null) {
                logger.debug("SegmentTracker created with write key: " + writeKey);

                // Now Analytics.with will return the custom instance
                analytics = new Analytics.Builder(context, writeKey)
                        .debugging(debugging)
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
    public void identify(String id, Traits traits, Options options) {
        try {
            if (analytics != null) {
                analytics.identify(id, traits, options);
            }
        } catch(Exception e) {
            logger.error(e);
        }
    }
    
    /**
     * This method is unused in GA release of the app.
     * @return
     */
    private AnalyticsContext getContext() {
        try {
            if (analytics != null) {
                AnalyticsContext analyticsContext = analytics.getAnalyticsContext(); 
                return analyticsContext;
            }
        } catch(Exception e) {
            logger.error(e);
        }
        
        return null;
    }
    
    private void logout() {
        try {
            if (analytics != null) {
                analytics.logout();
            }
        } catch(Exception e) {
            logger.error(e);
        }
    }
}
