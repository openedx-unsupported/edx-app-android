package org.edx.mobile.module.analytics;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class SegmentAnalytics implements Analytics {
    @Inject
    private SegmentTracker tracker;

    /**
     * Utility class that defines a specific format for an analytics event that we use in Segment.
     */
    private static class SegmentAnalyticsEvent {
        public Properties properties;
        public Properties data;

        public SegmentAnalyticsEvent() {
            this.properties = new Properties();
            this.data = new Properties();
            this.properties.putValue(Keys.DATA, this.data);

            setCustomProperties();
        }

        private void setCourseContext(String courseId, String unitUrl, String component) {
            this.properties.put(Keys.CONTEXT, getEventContext(courseId, unitUrl, component));
        }

        //This method sets app name in the context properties
        private void setAppNameContext() {
            this.properties.put(Keys.CONTEXT, getAppNameContext());
        }

        /**
         * Properties needed to be added with each analytics event will be done using this function
         * Currently, we are adding Google Analytics' custom dimensions using it
         */
        private void setCustomProperties() {
            // Device orientation dimension
            boolean isPortrait = MainApplication.instance().getResources()
                    .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            this.properties.putValue(Keys.DEVICE_ORIENTATION,
                    (isPortrait ? Values.PORTRAIT : Values.LANDSCAPE));
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
                cxtProps.putValue(Keys.COURSE_ID, courseId);
            }
            if (unitUrl != null) {
                cxtProps.putValue(Keys.OPEN_BROWSER, unitUrl);
            }
            if (component != null) {
                cxtProps.putValue(Keys.COMPONENT, component);
            }
            cxtProps.putValue(Keys.APP, Values.APP_NAME);

            return cxtProps;
        }
    }

    /**
     * Utility class that defines the basic structure for a Segment tracker that we can use for
     * sending analytics events.
     */
    private static class SegmentTracker {
        private com.segment.analytics.Analytics analytics;
        private final Logger logger = new Logger(getClass().getName());
        private Config config;

        @Inject
        public SegmentTracker(Context context, Config config) {
            this.config = config;
            String writeKey = config.getSegmentConfig().getSegmentWriteKey();
            boolean debugging = context.getResources().getBoolean(R.bool.analytics_debug);
            int queueSize = context.getResources().getInteger(R.integer.analytics_queue_size);
            int flushInterval = context.getResources().getInteger(R.integer.analytics_flush_interval);

            // Must be called before any calls to Analytics.with(context)
            // Now Analytics.with will return the custom instance

            if (writeKey != null) {
                logger.debug("SegmentTracker created with write key: " + writeKey);
                // Now Analytics.with will return the custom instance
                analytics = new com.segment.analytics.Analytics.Builder(context, writeKey)
                        .flushQueueSize(queueSize)
                        .flushInterval(flushInterval, TimeUnit.SECONDS)
                        .logLevel(debugging ? com.segment.analytics.Analytics.LogLevel.VERBOSE : com.segment.analytics.Analytics.LogLevel.NONE)
                        .build();
            } else {
                logger.warn("writeKey is null, Segment analytics will not work.");
            }
        }

        /**
         * This function is used to reset the user/
         * flush all remaining events for particular user
         */
        public void resetIdentifyUser() {
            if (analytics != null) {
                analytics.flush();
            }
        }

        /**
         * Calls track method of Analytics.
         *
         * @param event
         * @param props
         */
        public void track(String event, Properties props) {
            if (analytics != null) {
                analytics.track(event, props);
            }
        }

        /**
         * Calls screen method of Analytics.
         *
         * @param category
         * @param name
         */
        public void screen(String category, String name, Properties properties) {
            if (analytics != null) {
                analytics.screen(category, name, properties);
            }
        }

        /**
         * Calls identify method of Analytics.
         *
         * @param id
         * @param traits
         * @param options
         */
        public void identify(String id, Traits traits, Options options) {
            if (analytics != null) {
                analytics.identify(id, traits, options);
            }
        }
    }

    /**
     * This function is used to send the screen tracking event, with an extra event for
     * sending course id.
     *
     * @param screenName The screen name to track
     * @param courseId   course id of the course we are viewing
     * @param action     any custom action we need to send with event
     * @param values     any custom key-value pairs we need to send with event
     */
    @Override
    public void trackScreenView(@NonNull String screenName, @Nullable String courseId,
                                @Nullable String action,
                                @Nullable Map<String, String> values) {
        // Sending screen view
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.setAppNameContext();
        if (!TextUtils.isEmpty(action)) {
            aEvent.properties.put(Keys.ACTION, action);
        }
        if (!TextUtils.isEmpty(courseId)) {
            aEvent.properties.put(Keys.COURSE_ID, courseId);
        }
        if (values != null) {
            aEvent.data.putAll(values);
        }
        tracker.screen("", screenName, aEvent.properties);

        // Sending screen event
        addCategoryToBiEvents(aEvent.properties, Values.SCREEN, screenName);
        tracker.track(screenName, aEvent.properties);
    }

    /**
     * This function is used to track Video Loading
     *
     * @param videoId
     * @param courseId
     * @param unitUrl
     */
    @Override
    public void trackVideoLoading(String videoId, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_LOADED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.track(Events.LOADED_VIDEO, aEvent.properties);
    }

    /**
     * This function is used to track Video Playing
     *
     * @param videoId     -   Video Id that is being Played
     * @param currentTime -  Video Playing started at
     * @param unitUrl     -   Page Url for that Video
     * @param courseId    -     CourseId under which the video is present
     */
    @Override
    public void trackVideoPlaying(String videoId, Double currentTime,
                                  String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.VIDEO_PLAYED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.PLAYED_VIDEO, aEvent.properties);
    }

    /**
     * This function is used to track Video Pause
     *
     * @param videoId     -   Video Id that is being Played
     * @param currentTime -  Video Playing started at
     * @param courseId    -  CourseId under which the video is present
     * @param unitUrl     -   Page Url for that Video
     */
    @Override
    public void trackVideoPause(String videoId,
                                Double currentTime, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.VIDEO_PAUSED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.track(Events.PAUSED_VIDEO, aEvent.properties);
    }

    /**
     * This function is used to track Video Stop
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     */
    @Override
    public void trackVideoStop(String videoId, Double currentTime, String courseId,
                               String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.VIDEO_STOPPED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.STOPPED_VIDEO, aEvent.properties);
    }

    /**
     * This function is used to track 30 second rewind on Video
     *
     * @param videoId
     * @param oldTime
     * @param newTime
     * @param courseId
     * @param unitUrl
     * @param skipSeek
     */
    @Override
    public void trackVideoSeek(String videoId,
                               Double oldTime, Double newTime, String courseId, String unitUrl, Boolean skipSeek) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_SEEKED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        //Call the format Double value so that we can have upto 3 decimal places after
        oldTime = formatDoubleValue(oldTime, 3);
        newTime = formatDoubleValue(newTime, 3);
        Double skipInterval = newTime - oldTime;
        skipInterval = formatDoubleValue(skipInterval, 3);
        aEvent.data.putValue(Keys.OLD_TIME, oldTime);
        aEvent.data.putValue(Keys.NEW_TIME, newTime);
        if (skipSeek) {
            aEvent.data.putValue(Keys.SEEK_TYPE, Values.SKIP);
        } else {
            aEvent.data.putValue(Keys.SEEK_TYPE, Values.SLIDE);
        }
        aEvent.data.putValue(Keys.REQUESTED_SKIP_INTERVAL, skipInterval);

        tracker.track(Events.SEEK_VIDEO, aEvent.properties);
    }

    /**
     * This function is used to Show Transcript
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     */
    @Override
    public void trackShowTranscript(String videoId, Double currentTime, String courseId,
                                    String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.TRANSCRIPT_SHOWN);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.SHOW_TRANSCRIPT, aEvent.properties);
    }

    /**
     * This function is used to Hide Transcript
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public void trackHideTranscript(String videoId, Double currentTime, String courseId,
                                    String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.TRANSCRIPT_HIDDEN);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.HIDE_TRANSCRIPT, aEvent.properties);
    }

    /**
     * This resets the Identify user once the user has logged out
     */
    @Override
    public void resetIdentifyUser() {
        tracker.resetIdentifyUser();
    }

    /**
     * This function is used for getting common properties object having Module and Code
     *
     * @param videoId
     * @param eventName
     * @return The {@link SegmentAnalyticsEvent} updated with provided with arguments
     */
    private SegmentAnalyticsEvent getCommonProperties(String videoId, String eventName) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, eventName);
        if (videoId != null) {
            aEvent.data.putValue(Keys.MODULE_ID, videoId);
        }
        aEvent.data.putValue(Keys.CODE, Values.MOBILE);
        return aEvent;
    }

    /**
     * This function is used for getting common properties object having Module and Code and Current Time
     *
     * @param currentTime
     * @param videoId
     * @return The {@link SegmentAnalyticsEvent} updated with provided with arguments
     */
    private SegmentAnalyticsEvent getCommonPropertiesWithCurrentTime(Double currentTime,
                                                                     String videoId, String eventName) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, eventName);
        if (currentTime != null) {
            currentTime = formatDoubleValue(currentTime, 3);
            aEvent.data.putValue(Keys.CURRENT_TIME, currentTime);
        }
        return aEvent;
    }

    /**
     * This function returns decimals value for a Double
     *
     * @param value
     * @param places
     * @return The formatted {@link Double}
     */
    private Double formatDoubleValue(Double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * This function sets and returns the app name in Properties object
     *
     * @return A {@link Properties} object populated with app's name
     */
    private static Properties getAppNameContext() {
        Properties cxtProps = new Properties();
        cxtProps.putValue(Keys.APP, Values.APP_NAME);
        return cxtProps;
    }

    /**
     * This function is used to track Video Download completed
     *
     * @param videoId  -  Video id for which download has started
     * @param courseId
     * @param unitUrl
     */
    @Override
    public void trackDownloadComplete(String videoId, String courseId,
                                      String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_DOWNLOADED);
        aEvent.setCourseContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);

        tracker.track(Events.VIDEO_DOWNLOADED, aEvent.properties);
    }

    /**
     * This function is used to track Bulk Download from Sections
     *
     * @param section      -   Section in which the subsection is present
     * @param enrollmentId -  Course under which the subsection is present
     * @param videoCount   -  no of videos started downloading
     */
    @Override
    public void trackSectionBulkVideoDownload(String enrollmentId,
                                              String section, long videoCount) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        if (section != null) {
            aEvent.data.putValue(Keys.COURSE_SECTION, section);
        }
        aEvent.data.putValue(Keys.NO_OF_VIDEOS, videoCount);
        aEvent.properties.putValue(Keys.NAME, Values.BULKDOWNLOAD_SECTION);
        aEvent.setCourseContext(enrollmentId,
                null, Values.DOWNLOAD_MODULE);

        tracker.track(Events.BULK_DOWNLOAD_SECTION, aEvent.properties);
    }


    /**
     * This function is used to track Bulk Download from Subsection
     *
     * @param section      -   Section in which the subsection is present
     * @param subSection   -  Subsection from which the download started
     * @param enrollmentId -  Course under which the subsection is present
     * @param videoCount   -  no of videos started downloading
     */
    @Override
    public void trackSubSectionBulkVideoDownload(String section,
                                                 String subSection, String enrollmentId, long videoCount) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        if (section != null && subSection != null) {
            aEvent.data.putValue(Keys.COURSE_SECTION, section);
            aEvent.data.putValue(Keys.COURSE_SUBSECTION, subSection);
        }
        aEvent.data.putValue(Keys.NO_OF_VIDEOS, videoCount);
        aEvent.properties.putValue(Keys.NAME, Values.BULK_DOWNLOAD_SUBSECTION);
        aEvent.setCourseContext(enrollmentId,
                null, Values.DOWNLOAD_MODULE);

        tracker.track(Events.BULK_DOWNLOAD_SUBSECTION, aEvent.properties);
    }

    /**
     * This function is used to track Video Download started from Video List
     *
     * @param videoId  -  Video id for which download has started
     * @param courseId
     * @param unitUrl
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public void trackSingleVideoDownload(String videoId, String courseId,
                                         String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId,
                Values.SINGLE_VIDEO_DOWNLOAD);
        aEvent.setCourseContext(courseId,
                unitUrl, Values.DOWNLOAD_MODULE);
        tracker.track(Events.SINGLE_VIDEO_DOWNLOAD, aEvent.properties);
    }

    /**
     * This function is used to track Video Orientation
     *
     * @param videoId
     * @param currentTime
     * @param isLandscape -  true / false based on orientation
     * @param courseId
     * @param unitUrl
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public void trackVideoOrientation(String videoId, Double currentTime,
                                      boolean isLandscape, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.FULLSREEN_TOGGLED);
        aEvent.data.putValue(Keys.FULLSCREEN, isLandscape);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.SCREEN_TOGGLED, aEvent.properties);
    }

    @Override
    public void trackDiscoverCoursesClicked() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.DISCOVER_COURSES_CLICK);
        aEvent.setAppNameContext();
        tracker.track(Events.DISCOVER_COURSES, aEvent.properties);
    }

    @Override
    public void trackExploreSubjectsClicked() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.EXPLORE_SUBJECTS_CLICK);
        aEvent.setAppNameContext();
        tracker.track(Events.EXPLORE_SUBJECTS, aEvent.properties);
    }

    /**
     * This function is used to track User Login activity
     */
    @Override
    public void trackUserLogin(String method) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USERLOGIN);
        //More information regarding a track event should be under 'data'
        if (method != null) {
            aEvent.data.putValue(Keys.METHOD, method);
        }

        aEvent.setAppNameContext();
        tracker.track(Events.USER_LOGIN, aEvent.properties);
    }

    /**
     * This function is used to track user logout
     */
    @Override
    public void trackUserLogout() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USERLOGOUT);
        aEvent.setAppNameContext();
        tracker.track(Events.USER_LOGOUT, aEvent.properties);
    }

    /**
     * This function is used to track browser's launching
     *
     * @param url
     */
    @Override
    public void trackBrowserLaunched(String url) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.BROWSER_LAUNCHED);
        if (url != null) {
            aEvent.data.putValue(Keys.TARGET_URL, url);
        }
        aEvent.setAppNameContext();

        tracker.track(Events.BROWSER_LAUNCHED, aEvent.properties);
    }

    /**
     * This function is used to track Language changed for Transcripts
     */
    @Override
    public void trackTranscriptLanguage(String videoId,
                                        Double currentTime, String lang, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.TRANSCRIPT_LANGUAGE);
        aEvent.properties.putValue(Keys.LANGUAGE, lang);
        aEvent.setCourseContext(courseId, unitUrl,
                Values.VIDEOPLAYER);

        tracker.track(Events.LANGUAGE_CLICKED, aEvent.properties);
    }

    /**
     * This function is used to track if user clicks on Sign up on landing page
     */
    @Override
    public void trackUserSignUpForAccount() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USER_NO_ACCOUNT);
        aEvent.setAppNameContext();

        tracker.track(Events.SIGN_UP, aEvent.properties);

    }

    /**
     * This function is used to track if user clicks on Find Courses
     */
    @Override
    public void trackUserFindsCourses() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USER_FIND_COURSES);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.USER_ENGAGEMENT, Values.COURSE_DISCOVERY);
        tracker.track(Events.FIND_COURSES, aEvent.properties);
    }

    /**
     * This function is used to track if user clicks on Create Account on registration screen
     */
    @Override
    public void trackCreateAccountClicked(String appVersion, String source) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.CREATE_ACCOUNT_CLICK);
        if (!TextUtils.isEmpty(source))
            aEvent.properties.putValue(Keys.PROVIDER, source);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.CONVERSION, appVersion);
        tracker.track(Events.CREATE_ACCOUNT_CLICKED, aEvent.properties);
    }

    /**
     * This function is used to track if user clicks on Enroll in the FindCourses Activity
     *
     * @param courseId     - Course Id for which user selected enroll
     * @param email_opt_in - Flag to show user wants to opt in for email notification
     */
    @Override
    public void trackEnrollClicked(String courseId, boolean email_opt_in) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.data.putValue(Keys.COURSE_ID, courseId);
        aEvent.data.putValue(Keys.EMAIL_OPT_IN, email_opt_in);
        aEvent.properties.putValue(Keys.NAME, Values.USER_COURSE_ENROLL);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties, Values.CONVERSION, courseId);
        tracker.track(Events.ENROLL_COURSES, aEvent.properties);
    }

    //Tracking methods introduced by BNOTIONS

    @Override
    public void trackUserConnectionSpeed(String connectionType, float connectionSpeed) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.CONNECTION_SPEED);
        aEvent.data.putValue(Keys.CONNECTION_TYPE, connectionType);
        aEvent.data.putValue(Keys.CONNECTION_SPEED, connectionSpeed);

        aEvent.setAppNameContext();
        tracker.track(Events.SPEED, aEvent.properties);
    }

    @Override
    public void trackNotificationReceived(@Nullable String courseId) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.NOTIFICATION_RECEIVED);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties, Values.PUSH_NOTIFICATION, courseId);
        tracker.track(Events.PUSH_NOTIFICATION_RECEIVED, aEvent.properties);
    }

    @Override
    public void trackNotificationTapped(@Nullable String courseId) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.NOTIFICATION_TAPPED);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties, Values.PUSH_NOTIFICATION, courseId);
        tracker.track(Events.PUSH_NOTIFICATION_TAPPED, aEvent.properties);
    }

    @Override
    public void courseDetailShared(String courseId, String aboutUrl, String shareType) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_COURSE_DETAIL_SHARED);

        aEvent.data.putValue(Keys.NAME, courseId);
        aEvent.data.putValue(Keys.CATEGORY, Values.SOCIAL_SHARING);
        aEvent.data.putValue(Keys.URL, aboutUrl);
        aEvent.data.putValue(Keys.TYPE, shareType);
        aEvent.setAppNameContext();
        tracker.track(Events.SOCIAL_COURSE_DETAIL_SHARED, aEvent.properties);
    }

    @Override
    public void certificateShared(@NonNull String courseId, @NonNull String certificateUrl, @NonNull String shareType) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_CERTIFICATE_SHARED);
        aEvent.data.putValue(Keys.COURSE_ID, courseId);
        aEvent.data.putValue(Keys.CATEGORY, Values.SOCIAL_SHARING);
        aEvent.data.putValue(Keys.URL, certificateUrl);
        aEvent.data.putValue(Keys.TYPE, shareType);
        aEvent.setAppNameContext();
        tracker.track(Events.SOCIAL_CERTIFICATE_SHARED, aEvent.properties);
    }

    @Override
    public void trackCourseComponentViewed(String blockId, String courseId) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.COMPONENT_VIEWED);
        aEvent.data.putValue(Keys.BLOCK_ID, blockId);
        aEvent.data.putValue(Keys.COURSE_ID, courseId);

        aEvent.setAppNameContext();
        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.NAVIGATION, Keys.COMPONENT_VIEWED);
        tracker.track(Events.COMPONENT_VIEWED, aEvent.properties);
    }

    @Override
    public void trackBrowserLaunched(String blockId, String courseId, boolean isSupported) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.OPEN_IN_BROWSER);
        aEvent.data.putValue(Keys.BLOCK_ID, blockId);
        aEvent.data.putValue(Keys.COURSE_ID, courseId);
        aEvent.data.putValue(Keys.SUPPORTED, isSupported);

        aEvent.setAppNameContext();
        //Add category for Google Analytics
        String label = (isSupported ? Values.OPEN_IN_WEB_SUPPORTED : Values.OPEN_IN_WEB_NOT_SUPPORTED);
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.NAVIGATION, label);
        tracker.track(Events.OPEN_IN_BROWSER, aEvent.properties);
    }

    @Override
    public void trackProfileViewed(@NonNull String username) {
        final SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.PROFILE_VIEWED);
        aEvent.setAppNameContext();
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.PROFILE, username);
        tracker.track(Events.PROFILE_VIEWED, aEvent.properties);
    }

    @Override
    public void trackProfilePhotoSet(boolean fromCamera) {
        final SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.PROFILE_PHOTO_SET);
        aEvent.setAppNameContext();
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.PROFILE, fromCamera ? Values.CAMERA : Values.LIBRARY);
        tracker.track(Events.PROFILE_PHOTO_SET, aEvent.properties);
    }

    /**
     * This function is set to identify the user for subsequent calls
     *
     * @param userID   - User Id from the server
     * @param email    -  email of the user
     * @param username -  Username/email that the user uses for signing in
     */
    @Override
    public void identifyUser(String userID, String email, String username) {
        Traits traits = new Traits();
        traits.putEmail(email);
        traits.putUsername(username);
        tracker.identify(userID, traits, new Options());
    }

    /**
     * This method sets category and labels to BI events
     *
     * @param props
     * @param category
     * @param label
     * @return An updated {@link Properties} object with CATEGORY and LABEL
     */
    private Properties addCategoryToBiEvents(Properties props, String category, String label) {
        props.put(Keys.CATEGORY, category);
        props.put(Keys.LABEL, label);
        return props;
    }
}
