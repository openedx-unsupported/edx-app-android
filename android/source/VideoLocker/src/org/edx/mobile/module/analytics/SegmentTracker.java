package org.edx.mobile.module.analytics;

import org.edx.mobile.R;
import org.edx.mobile.util.Environment;
import org.edx.mobile.util.LogUtil;

import android.content.Context;

import com.segment.analytics.Analytics;
import com.segment.analytics.AnalyticsContext;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

public class SegmentTracker {
    
    private Analytics analytics;
    
    public SegmentTracker(Context context) {
        try {
            String writeKey = Environment.getInstance().getConfig().getSegmentIOWriteKey();
            String debugging = context.getString(R.string.analytics_debug);
            int queueSize = context.getResources().getInteger(R.integer.analytics_queue_size);
            int flushInterval = context.getResources().getInteger(R.integer.analytics_flush_interval);

            if(writeKey!=null) {
                LogUtil.log(getClass().getName(), "SegmentTracker created with write key: " + writeKey);
                // Now Analytics.with will return the custom instance
                analytics = new Analytics.Builder(context, writeKey)
                        .debugging(Boolean.parseBoolean(debugging))
                        .queueSize(queueSize)
                        .flushInterval(flushInterval)
                        .build();
            }
        } catch(RuntimeException ex) {
            ex.printStackTrace();
        } catch(Exception ex) {
            ex.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        
        return null;
    }
    
    private void logout() {
        try {
            if (analytics != null) {
                analytics.logout();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
