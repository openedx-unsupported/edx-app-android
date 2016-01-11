package org.edx.mobile.module.analytics;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.images.ShareUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Singleton
public class ISegmentImpl implements ISegment {

    @Inject
    private ISegmentTracker tracker;

    @Override
    public void setTracker(ISegmentTracker tracker) {
        this.tracker = tracker;
    }

    class SegmentAnalyticsEvent {
        public SegmentAnalyticsEvent() {
            this.properties = new Properties();
            this.data = new Properties();
            if (this.data != null) {
                this.properties.putValue(Keys.DATA, this.data);
            }

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

            // Current navigation mode dimension
            PrefManager.UserPrefManager userPrefManager =
                    new PrefManager.UserPrefManager(MainApplication.instance());
            boolean isVideoMode = userPrefManager.isUserPrefVideoModel();
            this.properties.putValue(Keys.NAVIGATION_MODE,
                    (isVideoMode ? Values.OUTLINE_MODE_VIDEO : Values.OUTLINE_MODE_FULL));
        }

        public Properties properties;
        public Properties data;
    }

    /**
     * This function is set to identify the user for subsequent calls
     *
     * @param userID   - User Id from the server
     * @param email    -  email of the user
     * @param username -  Username/email that the user uses for signing in
     * @return A {@link Traits} object populated with provided user info
     */
    @Override
    public Traits identifyUser(String userID, String email, String username) {
        Traits traits = new Traits();
        traits.putEmail(email);
        traits.putUsername(username);
        tracker.identify(userID, traits, new Options());
        return traits;
    }


    /**
     * This function is used to track Video Loading
     *
     * @param videoId
     * @param courseId
     * @param unitUrl
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackVideoLoading(String videoId, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_LOADED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.track(Events.LOADED_VIDEO, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track Video Playing
     *
     * @param videoId     -   Video Id that is being Played
     * @param currentTime -  Video Playing started at
     * @param unitUrl     -   Page Url for that Video
     * @param courseId    -     CourseId under which the video is present
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackVideoPlaying(String videoId, Double currentTime,
                                        String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.VIDEO_PLAYED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.PLAYED_VIDEO, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track Video Pause
     *
     * @param videoId     -   Video Id that is being Played
     * @param currentTime -  Video Playing started at
     * @param courseId    -  CourseId under which the video is present
     * @param unitUrl     -   Page Url for that Video
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackVideoPause(String videoId,
                                      Double currentTime, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.VIDEO_PAUSED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.track(Events.PAUSED_VIDEO, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track Video Stop
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackVideoStop(String videoId, Double currentTime, String courseId,
                                     String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.VIDEO_STOPPED);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.STOPPED_VIDEO, aEvent.properties);
        return aEvent.properties;
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
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackVideoSeek(String videoId,
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
        return aEvent.properties;
    }

    /**
     * This function is used to Show Transcript
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackShowTranscript(String videoId, Double currentTime, String courseId,
                                          String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.TRANSCRIPT_SHOWN);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.SHOW_TRANSCRIPT, aEvent.properties);
        return aEvent.properties;
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
    public Properties trackHideTranscript(String videoId, Double currentTime, String courseId,
                                          String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.TRANSCRIPT_HIDDEN);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.HIDE_TRANSCRIPT, aEvent.properties);
        return aEvent.properties;
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
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackDownloadComplete(String videoId, String courseId,
                                            String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_DOWNLOADED);
        aEvent.setCourseContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);

        tracker.track(Events.VIDEO_DOWNLOADED, aEvent.properties);
        return aEvent.properties;
    }


    /**
     * This function is used to track Bulk Download from Sections
     *
     * @param section      -   Section in which the subsection is present
     * @param enrollmentId -  Course under which the subsection is present
     * @param videoCount   -  no of videos started downloading
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackSectionBulkVideoDownload(String enrollmentId,
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
        return aEvent.properties;
    }


    /**
     * This function is used to track Bulk Download from Subsection
     *
     * @param section      -   Section in which the subsection is present
     * @param subSection   -  Subsection from which the download started
     * @param enrollmentId -  Course under which the subsection is present
     * @param videoCount   -  no of videos started downloading
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackSubSectionBulkVideoDownload(String section,
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
        return aEvent.properties;
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
    public Properties trackSingleVideoDownload(String videoId, String courseId,
                                               String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonProperties(videoId,
                Values.SINGLE_VIDEO_DOWNLOAD);
        aEvent.setCourseContext(courseId,
                unitUrl, Values.DOWNLOAD_MODULE);
        tracker.track(Events.SINGLE_VIDEO_DOWNLOAD, aEvent.properties);
        return aEvent.properties;
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
    public Properties trackVideoOrientation(String videoId, Double currentTime,
                                            boolean isLandscape, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.FULLSREEN_TOGGLED);
        aEvent.data.putValue(Keys.FULLSCREEN, isLandscape);
        aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

        tracker.track(Events.SCREEN_TOGGLED, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track User Login activity
     * Method will take the following inputs “Password”|”Google”|”Facebook”
     */
    @Override
    public Properties trackUserLogin(String method) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USERLOGIN);
        //More information regarding a track event should be under 'data'
        if (method != null) {
            aEvent.data.putValue(Keys.METHOD, method);
        }


        aEvent.setAppNameContext();
        tracker.track(Events.USER_LOGIN, aEvent.properties);
        return aEvent.properties;
    }


    /**
     * This function is used to track user logout
     *
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackUserLogout() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USERLOGOUT);
        aEvent.setAppNameContext();
        tracker.track(Events.USER_LOGOUT, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to send the screen tracking event.
     *
     * @param screenName The screen name to track
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackScreenView(String screenName) {
        return trackScreenView(screenName, null, null);
    }

    /**
     * This function is used to send the screen tracking event, with an extra event for
     * sending course id.
     *
     * @param screenName The screen name to track
     * @param courseId   course id of the course we are viewing
     * @param action     any custom action we need to send with event
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackScreenView(String screenName, String courseId, String action) {
        // Sending screen view
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.setAppNameContext();
        if (!TextUtils.isEmpty(action)) {
            aEvent.properties.put(Keys.ACTION, action);
        }
        if (!TextUtils.isEmpty(courseId)) {
            aEvent.properties.put(Keys.COURSE_ID, courseId);
        }
        tracker.screen("", screenName, aEvent.properties);

        // Sending screen event
        addCategoryToBiEvents(aEvent.properties, Values.SCREEN, screenName);
        tracker.track(screenName, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track Open in Browser
     *
     * @param url
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackOpenInBrowser(String url) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.BROWSER_LAUNCHED);
        if (url != null) {
            aEvent.data.putValue(Keys.TARGET_URL, url);
        }
        aEvent.setAppNameContext();

        tracker.track(Events.BROWSER_LAUNCHED, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track Language changed for Transcripts
     */
    @Override
    public Properties trackTranscriptLanguage(String videoId,
                                              Double currentTime, String lang, String courseId, String unitUrl) {
        SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                videoId, Values.TRANSCRIPT_LANGUAGE);
        aEvent.properties.putValue(Keys.LANGUAGE, lang);
        aEvent.setCourseContext(courseId, unitUrl,
                Values.VIDEOPLAYER);

        tracker.track(Events.LANGUAGE_CLICKED, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track if user clicks on Sign up on landing page
     */
    @Override
    public Properties trackUserSignUpForAccount() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USER_NO_ACCOUNT);
        aEvent.setAppNameContext();

        tracker.track(Events.SIGN_UP, aEvent.properties);
        return aEvent.properties;

    }

    /**
     * This function is used to track if user clicks on Find Courses
     *
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackUserFindsCourses() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USER_FIND_COURSES);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.USER_ENGAGEMENT, Values.COURSE_DISCOVERY);
        tracker.track(Events.FIND_COURSES, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track if user clicks on Create Account on registration screen
     *
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackCreateAccountClicked(String appVersion, String source) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.CREATE_ACCOUNT_CLICK);
        if (!TextUtils.isEmpty(source))
            aEvent.properties.putValue(Keys.PROVIDER, source);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.CONVERSION, appVersion);
        tracker.track(Events.CREATE_ACCOUNT_CLICKED, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used to track if user clicks on Enroll in the FindCourses Activity
     *
     * @param courseId     - Course Id for which user selected enroll
     * @param email_opt_in - Flag to show user wants to opt in for email notification
     * @return A {@link Properties} object populated with analytics-event info
     */
    @Override
    public Properties trackEnrollClicked(String courseId, boolean email_opt_in) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.data.putValue(Keys.COURSE_ID, courseId);
        aEvent.data.putValue(Keys.EMAIL_OPT_IN, email_opt_in);
        aEvent.properties.putValue(Keys.NAME, Values.USER_COURSE_ENROLL);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties, Values.CONVERSION, courseId);
        tracker.track(Events.ENROLL_COURSES, aEvent.properties);
        return aEvent.properties;
    }

    //Tracking methods introduced by BNOTIONS

    //We may want to explore the idea of adding cell carrier to the context.
    @Override
    public Properties trackUserCellConnection(String carrierName, boolean isZeroRated) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.CONNECTION_CELL);
        aEvent.data.putValue(Keys.CELL_CARRIER, carrierName);
        aEvent.data.putValue(Keys.CELL_ZERO_RATED, isZeroRated);

        aEvent.setAppNameContext();
        tracker.track(Events.TRACK_CELL_CONNECTION, aEvent.properties);
        return aEvent.properties;

    }

    @Override
    public Properties trackUserConnectionSpeed(String connectionType, float connectionSpeed) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.CONNECTION_SPEED);
        aEvent.data.putValue(Keys.CONNECTION_TYPE, connectionType);
        aEvent.data.putValue(Keys.CONNECTION_SPEED, connectionSpeed);

        aEvent.setAppNameContext();
        tracker.track(Events.SPEED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties trackNotificationReceived(@Nullable String courseId) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.NOTIFICATION_RECEIVED);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties, Values.PUSH_NOTIFICATION, courseId);
        tracker.track(Events.PUSH_NOTIFICATION_RECEIVED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties trackNotificationTapped(@Nullable String courseId) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.NOTIFICATION_TAPPED);
        aEvent.setAppNameContext();

        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties, Values.PUSH_NOTIFICATION, courseId);
        tracker.track(Events.PUSH_NOTIFICATION_TAPPED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties courseGroupAccessed(String courseName) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.ACCESS_COURSE_GROUP);

        aEvent.data.putValue(Keys.COURSE_ID, courseName);

        aEvent.setAppNameContext();
        tracker.track(Events.COURSE_GROUP_ACCESSED, aEvent.properties);
        return aEvent.properties;

    }

    @Override
    public Properties gameGroupAccessed(long groupID, int groupUserCount) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.ACCESS_GAME_GROUP);

        aEvent.data.putValue(Keys.GROUP_ID, groupID);
        aEvent.data.putValue(Keys.GROUP_USER_COUNT, groupUserCount);

        aEvent.setAppNameContext();
        tracker.track(Events.GAME_GROUP_ACCESSED, aEvent.properties);
        return aEvent.properties;

    }

    @Override
    public Properties groupCreated(long groupID, int invitedUserCount) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.CREATE_GAME_GROUP);

        aEvent.data.putValue(Keys.GROUP_ID, groupID);
        aEvent.data.putValue(Keys.GROUP_INVITED_USER_COUNT, invitedUserCount);

        aEvent.setAppNameContext();
        tracker.track(Events.GAME_GROUP_CREATE, aEvent.properties);
        return aEvent.properties;

    }

    @Override
    public Properties groupInvited(long groupID, int invitedUserCount) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.INVITE_GAME_GROUP);

        aEvent.data.putValue(Keys.GROUP_ID, groupID);
        aEvent.data.putValue(Keys.GROUP_INVITED_USER_COUNT, invitedUserCount);

        aEvent.setAppNameContext();
        tracker.track(Events.GAME_GROUP_INVITE, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties courseShared(String courseName, String socialNetwork) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_COURSE_SHARED);

        aEvent.data.putValue(Keys.COURSE_ID, courseName);
        aEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

        aEvent.setAppNameContext();
        tracker.track(Events.SOCIAL_COURSE_SHARED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties courseDetailShared(String courseId, String aboutUrl, ShareUtils.ShareType shareType) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_COURSE_DETAIL_SHARED);

        aEvent.data.putValue(Keys.NAME, courseId);
        aEvent.data.putValue(Keys.CATEGORY, Values.SOCIAL_SHARING);
        aEvent.data.putValue(Keys.URL, aboutUrl);
        aEvent.data.putValue(Keys.TYPE, getShareTypeValue(shareType));
        aEvent.setAppNameContext();
        tracker.track(Events.SOCIAL_COURSE_DETAIL_SHARED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties certificateShared(@NonNull String courseId, @NonNull String certificateUrl, @NonNull ShareUtils.ShareType shareType) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_CERTIFICATE_SHARED);
        aEvent.data.putValue(Keys.COURSE_ID, courseId);
        aEvent.data.putValue(Keys.CATEGORY, Values.SOCIAL_SHARING);
        aEvent.data.putValue(Keys.URL, certificateUrl);
        aEvent.data.putValue(Keys.TYPE, getShareTypeValue(shareType));
        aEvent.setAppNameContext();
        tracker.track(Events.SOCIAL_CERTIFICATE_SHARED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties socialConnectionEvent(boolean connected, String socialNetwork) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_CONNECTION_CHANGE);

        aEvent.data.putValue(Keys.SOCIAL_CONNECTION_STATE, connected);
        aEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

        aEvent.setAppNameContext();
        tracker.track(Events.SOCIAL_CONNECTION_CHANGE, aEvent.properties);
        return aEvent.properties;

    }

    @Override
    public Properties coursesVisibleToFriendsChange(boolean visible) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SETTING_COURSES_VISIBLE_CHANGE);

        aEvent.data.putValue(Keys.SETTING_COURSES_VISIBLE_STATE, visible);

        aEvent.setAppNameContext();
        tracker.track(Events.SETTING_COURSES_VISIBLE_CHANGE, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties trackCourseOutlineMode(boolean isVideoMode) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.SWITCH_OUTLINE_MODE);

        aEvent.setAppNameContext();
        //Add category for Google Analytics
        String label = (isVideoMode ? Values.SWITCH_TO_VIDEO_MODE : Values.SWITCH_TO_FULL_MODE);
        aEvent.properties = addCategoryToBiEvents(aEvent.properties, Values.NAVIGATION, label);
        tracker.track(Events.SWITCH_OUTLINE_MODE, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties trackCourseComponentViewed(String blockId, String courseId) {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.COMPONENT_VIEWED);
        aEvent.data.putValue(Keys.BLOCK_ID, blockId);
        aEvent.data.putValue(Keys.COURSE_ID, courseId);

        aEvent.setAppNameContext();
        //Add category for Google Analytics
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.NAVIGATION, Keys.COMPONENT_VIEWED);
        tracker.track(Events.COMPONENT_VIEWED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties trackOpenInBrowser(String blockId, String courseId, boolean isSupported) {
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
        return aEvent.properties;
    }

    @Override
    public Properties trackProfileViewed(@NonNull String username) {
        final SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.PROFILE_VIEWED);
        aEvent.setAppNameContext();
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.PROFILE, username);
        tracker.track(Events.PROFILE_VIEWED, aEvent.properties);
        return aEvent.properties;
    }

    @Override
    public Properties trackProfilePhotoSet(boolean fromCamera) {
        final SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.PROFILE_PHOTO_SET);
        aEvent.setAppNameContext();
        aEvent.properties = addCategoryToBiEvents(aEvent.properties,
                Values.PROFILE, fromCamera ? Values.CAMERA : Values.LIBRARY);
        tracker.track(Events.PROFILE_PHOTO_SET, aEvent.properties);
        return aEvent.properties;
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

    public static String getShareTypeValue(@NonNull ShareUtils.ShareType shareType) {
        switch (shareType) {
            case FACEBOOK:
                return "facebook";
            case TWITTER:
                return "twitter";
            default:
                return "other";
        }
    }
}
