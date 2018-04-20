package org.edx.mobile.module.analytics;

import android.content.res.Configuration;

import com.segment.analytics.Properties;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.LoginPrefs;

/**
 * Utility class that defines a specific format for an analytics event that we deliver to Segment.
 */
public class SegmentEvent {
    public Properties properties;
    public Properties data;

    public SegmentEvent() {
        this.properties = new Properties();
        this.data = new Properties();
        this.properties.putValue(Analytics.Keys.DATA, this.data);

        setCustomProperties();

        // Set app name in the context properties
        Properties cxtProps = new Properties();
        cxtProps.putValue(Analytics.Keys.APP, Analytics.Values.APP_NAME);
        this.properties.put(Analytics.Keys.CONTEXT, cxtProps);

        // Set User ID in each event if a user is logged-in
        final ProfileModel profile = new LoginPrefs(MainApplication.instance()).getCurrentUserProfile();
        if (profile != null) {
            this.properties.putValue(Analytics.Keys.USER_ID, profile.id);
        }
    }

    public void setCourseContext(String courseId, String unitUrl, String component) {
        this.properties.put(Analytics.Keys.CONTEXT, getEventContext(courseId, unitUrl, component));
    }

    /**
     * Properties needed to be added with each analytics event will be done using this function
     * Currently, we are adding Google Analytics' custom dimensions using it
     */
    private void setCustomProperties() {
        // Device orientation dimension
        boolean isPortrait = MainApplication.instance().getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        this.properties.putValue(Analytics.Keys.DEVICE_ORIENTATION,
                (isPortrait ? Analytics.Values.PORTRAIT : Analytics.Values.LANDSCAPE));
    }

    /**
     * This function sets the Context values of values passed
     *
     * @param courseId
     * @param unitUrl
     * @param component
     * @return A {@link Properties} object populated with analytics-event info
     */
    private Properties getEventContext(String courseId, String unitUrl, String component) {
        Properties cxtProps = new Properties();
        if (courseId != null) {
            cxtProps.putValue(Analytics.Keys.COURSE_ID, courseId);
        }
        if (unitUrl != null) {
            cxtProps.putValue(Analytics.Keys.OPEN_BROWSER, unitUrl);
        }
        if (component != null) {
            cxtProps.putValue(Analytics.Keys.COMPONENT, component);
        }
        cxtProps.putValue(Analytics.Keys.APP, Analytics.Values.APP_NAME);

        return cxtProps;
    }
}
