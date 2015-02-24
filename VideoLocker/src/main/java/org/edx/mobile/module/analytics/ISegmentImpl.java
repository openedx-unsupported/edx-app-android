package org.edx.mobile.module.analytics;

import android.content.Context;

import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.logger.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;

class ISegmentImpl implements ISegment {

    private ISegmentTracker tracker;
    private final Logger logger = new Logger(getClass().getName());

    /**
     * This Constructor also initializes {@link ISegmentTrackerImpl}
     * that is used to submit analytics data.
     * @param context
     */
    ISegmentImpl(Context context) {
        this.tracker = new ISegmentTrackerImpl(context);
    }

    @Override
    public void setTracker(ISegmentTracker tracker) {
        this.tracker = tracker;
    }

    class SegmentAnalyticsEvent {
        public SegmentAnalyticsEvent() {
            this.properties = new Properties();
            this.data = new Properties();
            if(this.data!=null){
                this.properties.putValue(Keys.DATA, this.data);
            }
        }

        public void setCourseContext(String courseId, String unitUrl, String component) {
            this.properties.put(Keys.CONTEXT, getEventContext(courseId, unitUrl, component));
        }

        //This method sets app name in the context properties
        public void setAppNameContext() {
            this.properties.put(Keys.CONTEXT, getAppNameContext());
        }

        public Properties properties;
        public Properties data;
    }

    /**
     * This function is set to identify the user for subsequent calls
     * @param userID - User Id from the server
     * @param email  -  email of the user
     * @param username  -  Username/email that the user uses for signing in
     *
     * @return
     */
    @Override
    public Traits identifyUser(String userID, String email, String username) {
        try{
            Traits traits = new Traits();
            traits.putEmail(email);
            traits.putUsername(username);
            tracker.identify(userID, traits, new Options());
            return traits;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }


    /**
     * This function is used to track Video Loading
     * @param videoId
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackVideoLoading(String videoId, String courseId, String unitUrl){
        try{
            SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_LOADED);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
            tracker.track(Keys.LOADED_VIDEO, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
            return null;
        }
    }

    /**
     * This function is used to track Video Playing 
     * @param videoId   -   Video Id that is being Played
     * @param currentTime  -  Video Playing started at 
     * @param unitUrl     -   Page Url for that Video
     * @param courseId  -     CourseId under which the video is present
     * @return 
     */
    @Override
    public Properties trackVideoPlaying(String videoId, Double currentTime,
            String courseId, String unitUrl){
        try{
            SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.VIDEO_PLAYED);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.PLAYED_VIDEO , aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track Video Pause 
     * @param videoId   -   Video Id that is being Played
     * @param currentTime  -  Video Playing started at
     * @param courseId  -  CourseId under which the video is present
     * @param unitUrl     -   Page Url for that Video
     * @return 
     */
    @Override
    public Properties trackVideoPause(String videoId, 
            Double currentTime, String courseId, String unitUrl){
        try{
            SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.VIDEO_PAUSED);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
            tracker.track(Keys.PAUSED_VIDEO, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track Video Stop
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackVideoStop(String videoId, Double currentTime, String courseId,
            String unitUrl){
        try{
            SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.VIDEO_STOPPED);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.STOPPED_VIDEO, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }


    /**
     * This function is used to track 30 second rewind on Video 
     * @param videoId
     * @param oldTime
     * @param newTime
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackVideoSeek(String videoId,
            Double oldTime, Double newTime, String courseId, String unitUrl){
        try{
            SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_SEEKED);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
            //Call the format Double value so that we can have upto 3 decimal places after
            oldTime = formatDoubleValue(oldTime, 3);
            newTime = formatDoubleValue(newTime, 3);
            Double skipInterval = newTime - oldTime;
            skipInterval = formatDoubleValue(skipInterval, 3);
            aEvent.data.putValue(Keys.OLD_TIME, oldTime);
            aEvent.data.putValue(Keys.NEW_TIME, newTime);
            aEvent.data.putValue(Keys.SEEK_TYPE, Values.SKIP);
            aEvent.data.putValue(Keys.REQUESTED_SKIP_INTERVAL, skipInterval);

            tracker.track(Keys.SEEK_VIDEO, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to Show Transcript 
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackShowTranscript(String videoId, Double currentTime, String courseId,
            String unitUrl){
        try{
            SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.TRANSCRIPT_SHOWN);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.SHOW_TRANSCRIPT, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }


    /**
     * This function is used to Hide Transcript
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackHideTranscript(String videoId, Double currentTime ,String courseId,
            String unitUrl){
        try{
            SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                    videoId, Values.TRANSCRIPT_HIDDEN);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.HIDE_TRANSCRIPT, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
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
     * @param videoId
     * @return
     */
    private SegmentAnalyticsEvent getCommonProperties(String videoId, String eventName){
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, eventName);
            if(videoId!=null){
                aEvent.data.putValue(Keys.MODULE_ID, videoId);
            }
            aEvent.data.putValue(Keys.CODE, Values.MOBILE);
            return aEvent;
        }catch(Exception e){
            logger.error(e);
        }
        return new SegmentAnalyticsEvent();
    }

    /**
     * This function is used for getting common properties object having Module and Code and Current Time
     * @param currentTime
     * @param videoId
     * @return
     */
    private SegmentAnalyticsEvent getCommonPropertiesWithCurrentTime(Double currentTime, 
            String videoId, String eventName){
        try{
            SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, eventName);
            if(currentTime!=null){
                currentTime = formatDoubleValue(currentTime,3);
                aEvent.data.putValue(Keys.CURRENT_TIME, currentTime);
            }
            return aEvent;
        }catch(Exception e){
            logger.error(e);
        }
        return new SegmentAnalyticsEvent();
    }

    /**
     * This function returns decimals value for a Double
     * @param value
     * @param places
     * @return
     */
    private Double formatDoubleValue(Double value, int places){
        try{
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }catch(Exception e){
            logger.error(e);
        }
        return 0.00;
    }

    /**
     * This function sets the Context values of values passed
     * @param courseId
     * @param unitUrl
     */
    private Properties getEventContext(String courseId, String unitUrl, String component){
        try{
            Properties cxtProps = new Properties();
            if(courseId!=null){
                cxtProps.putValue(Keys.COURSE_ID, courseId);
            }
            if(unitUrl!=null){
                cxtProps.putValue(Keys.OPEN_BROWSER, unitUrl);
            }
            if(component!=null){
                cxtProps.putValue(Keys.COMPONENT, component);
            }
            cxtProps.putValue(Keys.APP, Values.APP_NAME);

            return cxtProps;
        }catch(Exception e){
            logger.error(e);
            return new Properties();
        }
    }

    /**
     * This function sets and returns the app name in Properties object
     */
    private static Properties getAppNameContext(){
            Properties cxtProps = new Properties();
            cxtProps.putValue(Keys.APP, Values.APP_NAME);
            return cxtProps;
    }

    /**
     * This function is used to track Video Download completed 
     * @param videoId  -  Video id for which download has started
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackDownloadComplete(String videoId, String courseId,
            String unitUrl) {
        try{
            SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, Values.VIDEO_DOWNLOADED);
            aEvent.setCourseContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);

            tracker.track(Keys.VIDEO_DOWNLOADED, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }


    /**
     * This function is used to track Bulk Download from Sections
     * @param section  -   Section in which the subsection is present 
     * @param enrollmentId  -  Course under which the subsection is present
     * @param videoCount  -  no of videos started downloading
     * @return 
     */
    @Override
    public Properties trackSectionBulkVideoDownload(String enrollmentId,
            String section, long videoCount) {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            if(section!=null){
                aEvent.data.putValue(Keys.COURSE_SECTION, section);
            }
            aEvent.data.putValue(Keys.NO_OF_VIDEOS, videoCount);
            aEvent.properties.putValue(Keys.NAME, Values.BULKDOWNLOAD_SECTION);
            aEvent.setCourseContext(enrollmentId,
                    null, Values.DOWNLOAD_MODULE);

            tracker.track(Keys.BULK_DOWNLOAD_SECTION, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }


    /**
     * This function is used to track Bulk Download from Subsection
     * @param section  -   Section in which the subsection is present 
     * @param subSection -  Subsection from which the download started 
     * @param enrollmentId  -  Course under which the subsection is present
     * @param videoCount  -  no of videos started downloading
     * @return 
     */
    @Override
    public Properties trackSubSectionBulkVideoDownload(String section,
            String subSection, String enrollmentId, long videoCount) {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            if(section!=null && subSection!=null){
                aEvent.data.putValue(Keys.COURSE_SECTION, section);
                aEvent.data.putValue(Keys.COURSE_SUBSECTION, subSection);
            }
            aEvent.data.putValue(Keys.NO_OF_VIDEOS, videoCount);
            aEvent.properties.putValue(Keys.NAME, Values.BULK_DOWNLOAD_SUBSECTION);
            aEvent.setCourseContext(enrollmentId,
                    null, Values.DOWNLOAD_MODULE);

            tracker.track(Keys.BULK_DOWNLOAD_SUBSECTION, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track Video Download started from Video List 
     * @param videoId  -  Video id for which download has started
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackSingleVideoDownload(String videoId, String courseId,
            String unitUrl) {
        try{
            SegmentAnalyticsEvent aEvent = getCommonProperties(videoId, 
                    Values.SINGLE_VIDEO_DOWNLOAD);
            aEvent.setCourseContext(courseId,
                    unitUrl, Values.DOWNLOAD_MODULE);
            tracker.track(Keys.SINGLE_VIDEO_DOWNLOAD, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track Video Orientation
     * @param videoId
     * @param currentTime
     * @param isLandscape -  true / false based on orientation
     * @param courseId
     * @param unitUrl
     * @return 
     */
    @Override
    public Properties trackVideoOrientation(String videoId, Double currentTime,
            boolean isLandscape, String courseId, String unitUrl) {
        try{
            SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.FULLSREEN_TOGGLED);
            aEvent.data.putValue(Keys.FULLSCREEN, isLandscape);
            aEvent.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.SCREEN_TOGGLED, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }

        return null;
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
        if(method!=null){
            aEvent.data.putValue(Keys.METHOD, method);
        }


        aEvent.setAppNameContext();
        tracker.track(Keys.USER_LOGIN, aEvent.properties);
        return aEvent.properties;
    }


    /**
     * This function is used to track user logout
     * @return 
     */
    @Override
    public Properties trackUserLogout() {
        SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
        aEvent.properties.putValue(Keys.NAME, Values.USERLOGOUT);
        aEvent.setAppNameContext();
        tracker.track(Keys.USER_LOGOUT, aEvent.properties);
        return aEvent.properties;
    }

    /**
     * This function is used send Screen View Details
     * @param screenName
     * @return 
     */
    @Override
    public Properties screenViewsTracking(String screenName) {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.setAppNameContext();
            tracker.screen("", screenName, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
            return null;
        }
    }

    /**
     * This function is used to track Open in Browser
     * @param url
     * @return 
     */
    @Override
    public Properties trackOpenInBrowser(String url) {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.BROWSER_LAUNCHED);
            if(url!=null){
                aEvent.data.putValue(Keys.TARGET_URL, url);
            }
            aEvent.setAppNameContext();

            tracker.track(Keys.BROWSER_LAUNCHED, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track Language changed for Transcripts
     */
    @Override
    public Properties trackTranscriptLanguage(String videoId,
            Double currentTime, String lang, String courseId, String unitUrl) {
        try{
            SegmentAnalyticsEvent aEvent = getCommonPropertiesWithCurrentTime(currentTime,
                    videoId, Values.TRANSCRIPT_LANGUAGE);
            aEvent.properties.putValue(Keys.LANGUAGE, lang);
            aEvent.setCourseContext(courseId, unitUrl,
                    Values.VIDEOPLAYER);

            tracker.track(Keys.LANGUAGE_CLICKED, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track if user clicks on Sign up on landing page
     */
    @Override
    public Properties trackUserSignUpForAccount() {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.USER_NO_ACCOUNT);
            aEvent.setAppNameContext();

            tracker.track(Keys.SIGN_UP, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    /**
     * This function is used to track if user clicks on Find Courses
     * @return
     */
    @Override
    public Properties trackUserFindsCourses() {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.USER_FIND_COURSES);
            aEvent.setAppNameContext();

            tracker.track(Keys.FIND_COURSES, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track if user clicks on Create Account on registration screen
     * @return
     */
    @Override
    public Properties trackCreateAccountClicked() {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.CREATE_ACCOUNT_CLICK);
            aEvent.setAppNameContext();

            tracker.track(Keys.CREATE_ACCOUNT_CLICKED, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * This function is used to track if user clicks on Enroll in the FindCourses Activity
     * @return
     * @param courseId - Course Id for which user selected enroll
     * @param email_opt_in - Flag to show user wants to opt in for email notification
     * @return
     */
    @Override
    public Properties trackEnrollClicked(String courseId, boolean email_opt_in) {
        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.data.putValue(Keys.COURSE_ID, courseId);
            aEvent.data.putValue(Keys.EMAIL_OPT_IN, email_opt_in);
            aEvent.properties.putValue(Keys.NAME, Values.USER_COURSE_ENROLL);
            aEvent.setAppNameContext();

            tracker.track(Keys.ENROLL_COURSES, aEvent.properties);
            return aEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    //Tracking methods introduced by BNOTIONS

    //We may want to explore the idea of adding cell carrier to the context.
    @Override
    public Properties trackUserCellConnection(String carrierName, boolean isZeroRated) {

        try{
            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.CONNECTION_CELL);
            aEvent.data.putValue(Keys.CELL_CARRIER, carrierName);
            aEvent.data.putValue(Keys.CELL_ZERO_RATED, isZeroRated);

            aEvent.setAppNameContext();
            tracker.track(Keys.TRACK_CELL_CONNECTION, aEvent.properties);
            return aEvent.properties;

        }catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties trackUserConnectionSpeed(String connectionType, float connectionSpeed) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.CONNECTION_SPEED);
            aEvent.data.putValue(Keys.CONNECTION_TYPE, connectionType);
            aEvent.data.putValue(Keys.CONNECTION_SPEED, connectionSpeed);

            aEvent.setAppNameContext();
            tracker.track(Keys.SPEED, aEvent.properties);
            return aEvent.properties;

        }catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties courseGroupAccessed(String courseName) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.ACCESS_COURSE_GROUP);

            aEvent.data.putValue(Keys.COURSE_ID, courseName);

            aEvent.setAppNameContext();
            tracker.track(Keys.COURSE_GROUP_ACCESSED, aEvent.properties);
            return aEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties gameGroupAccessed(long groupID, int groupUserCount) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.ACCESS_GAME_GROUP);

            aEvent.data.putValue(Keys.GROUP_ID, groupID);
            aEvent.data.putValue(Keys.GROUP_USER_COUNT, groupUserCount);

            aEvent.setAppNameContext();
            tracker.track(Keys.GAME_GROUP_ACCESSED, aEvent.properties);
            return aEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties groupCreated(long groupID, int invitedUserCount) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.CREATE_GAME_GROUP);

            aEvent.data.putValue(Keys.GROUP_ID, groupID);
            aEvent.data.putValue(Keys.GROUP_INVITED_USER_COUNT, invitedUserCount);

            aEvent.setAppNameContext();
            tracker.track(Keys.GAME_GROUP_CREATE, aEvent.properties);
            return aEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties groupInvited(long groupID, int invitedUserCount){

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.INVITE_GAME_GROUP);

            aEvent.data.putValue(Keys.GROUP_ID, groupID);
            aEvent.data.putValue(Keys.GROUP_INVITED_USER_COUNT, invitedUserCount);

            aEvent.setAppNameContext();
            tracker.track(Keys.GAME_GROUP_INVITE, aEvent.properties);
            return aEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties courseShared(String courseName, String socialNetwork) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_COURSE_SHARED);

            aEvent.data.putValue(Keys.COURSE_ID, courseName);
            aEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

            aEvent.setAppNameContext();
            tracker.track(Keys.SOCIAL_COURSE_SHARED, aEvent.properties);
            return aEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;


    }

    @Override
    public Properties certificateShared(String courseName, String socialNetwork) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_CERTIFICATE_SHARED);

            aEvent.data.putValue(Keys.COURSE_ID, courseName);
            aEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

            aEvent.setAppNameContext();
            tracker.track(Keys.SOCIAL_CERTIFICATE_SHARED, aEvent.properties);
            return aEvent.properties;


        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties socialConnectionEvent(boolean connected, String socialNetwork) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.SOCIAL_CONNECTION_CHANGE);

            aEvent.data.putValue(Keys.SOCIAL_CONNECTION_STATE, connected);
            aEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

            aEvent.setAppNameContext();
            tracker.track(Keys.SOCIAL_CONNECTION_CHANGE, aEvent.properties);
            return aEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties coursesVisibleToFriendsChange(boolean visible) {

        try {

            SegmentAnalyticsEvent aEvent = new SegmentAnalyticsEvent();
            aEvent.properties.putValue(Keys.NAME, Values.SETTING_COURSES_VISIBLE_CHANGE);

            aEvent.data.putValue(Keys.SETTING_COURSES_VISIBLE_STATE, visible);

            aEvent.setAppNameContext();
            tracker.track(Keys.SETTING_COURSES_VISIBLE_CHANGE, aEvent.properties);
            return aEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

}
