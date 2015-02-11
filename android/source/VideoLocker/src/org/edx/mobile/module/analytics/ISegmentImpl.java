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

    class VideoEvent {
        public VideoEvent() {
            this.properties = new Properties();
            this.data = new Properties();
            if(this.data!=null){
                this.properties.putValue(Keys.DATA, this.data);
            }
        }

        public void setContext(String courseId, String unitUrl, String component) {
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
            VideoEvent vEvent = getCommonProperties(videoId, Values.VIDEO_LOADED);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);
            tracker.track(Keys.LOADED_VIDEO, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.VIDEO_PLAYED);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.PLAYED_VIDEO , vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.VIDEO_PAUSED);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);
            tracker.track(Keys.PAUSED_VIDEO, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.VIDEO_STOPPED);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.STOPPED_VIDEO, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonProperties(videoId, Values.VIDEO_SEEKED);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);
            //Call the format Double value so that we can have upto 3 decimal places after
            oldTime = formatDoubleValue(oldTime, 3);
            newTime = formatDoubleValue(newTime, 3);
            Double skipInterval = newTime - oldTime;
            skipInterval = formatDoubleValue(skipInterval, 3);
            vEvent.data.putValue(Keys.OLD_TIME, oldTime);
            vEvent.data.putValue(Keys.NEW_TIME, newTime);
            vEvent.data.putValue(Keys.SEEK_TYPE, Values.SKIP);
            vEvent.data.putValue(Keys.REQUESTED_SKIP_INTERVAL, skipInterval);

            tracker.track(Keys.SEEK_VIDEO, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.TRANSCRIPT_SHOWN);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.SHOW_TRANSCRIPT, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonPropertiesWithCurrentTime(currentTime,
                    videoId, Values.TRANSCRIPT_HIDDEN);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.HIDE_TRANSCRIPT, vEvent.properties);
            return vEvent.properties;
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
    private VideoEvent getCommonProperties(String videoId, String eventName){
        try{
            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, eventName);
            if(videoId!=null){
                vEvent.data.putValue(Keys.MODULE_ID, videoId);
            }
            vEvent.data.putValue(Keys.CODE, Values.MOBILE);
            return vEvent;
        }catch(Exception e){
            logger.error(e);
        }
        return new VideoEvent();
    }

    /**
     * This function is used for getting common properties object having Module and Code and Current Time
     * @param currentTime
     * @param videoId
     * @return
     */
    private VideoEvent getCommonPropertiesWithCurrentTime(Double currentTime, 
            String videoId, String eventName){
        try{
            VideoEvent vEvent = getCommonProperties(videoId, eventName);
            if(currentTime!=null){
                currentTime = formatDoubleValue(currentTime,3);
                vEvent.data.putValue(Keys.CURRENT_TIME, currentTime);
            }
            return vEvent;
        }catch(Exception e){
            logger.error(e);
        }
        return new VideoEvent();
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
            VideoEvent vEvent = getCommonProperties(videoId, Values.VIDEO_DOWNLOADED);
            vEvent.setContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);

            tracker.track(Keys.VIDEO_DOWNLOADED, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = new VideoEvent();
            if(section!=null){
                vEvent.data.putValue(Keys.COURSE_SECTION, section);
            }
            vEvent.data.putValue(Keys.NO_OF_VIDEOS, videoCount);
            vEvent.properties.putValue(Keys.NAME, Values.BULKDOWNLOAD_SECTION);
            vEvent.setContext(enrollmentId,
                    null, Values.DOWNLOAD_MODULE);

            tracker.track(Keys.BULK_DOWNLOAD_SECTION, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = new VideoEvent();
            if(section!=null && subSection!=null){
                vEvent.data.putValue(Keys.COURSE_SECTION, section);
                vEvent.data.putValue(Keys.COURSE_SUBSECTION, subSection);
            }
            vEvent.data.putValue(Keys.NO_OF_VIDEOS, videoCount);
            vEvent.properties.putValue(Keys.NAME, Values.BULK_DOWNLOAD_SUBSECTION);
            vEvent.setContext(enrollmentId,
                    null, Values.DOWNLOAD_MODULE);

            tracker.track(Keys.BULK_DOWNLOAD_SUBSECTION, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonProperties(videoId, 
                    Values.SINGLE_VIDEO_DOWNLOAD);
            vEvent.setContext(courseId, 
                    unitUrl, Values.DOWNLOAD_MODULE);
            tracker.track(Keys.SINGLE_VIDEO_DOWNLOAD, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonPropertiesWithCurrentTime(currentTime, 
                    videoId, Values.FULLSREEN_TOGGLED);
            vEvent.data.putValue(Keys.FULLSCREEN, isLandscape);
            vEvent.setContext(courseId, unitUrl, Values.VIDEOPLAYER);

            tracker.track(Keys.SCREEN_TOGGLED, vEvent.properties);
            return vEvent.properties;
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
        VideoEvent vEvent = new VideoEvent();
        vEvent.properties.putValue(Keys.NAME, Values.USERLOGIN);
        //More information regarding a track event should be under 'data'
        if(method!=null){
            vEvent.data.putValue(Keys.METHOD, method);
        }


        vEvent.setAppNameContext();
        tracker.track(Keys.USER_LOGIN, vEvent.properties);
        return vEvent.properties;
    }


    /**
     * This function is used to track user logout
     * @return 
     */
    @Override
    public Properties trackUserLogout() {
        VideoEvent vEvent = new VideoEvent();
        vEvent.properties.putValue(Keys.NAME, Values.USERLOGOUT);
        vEvent.setAppNameContext();
        tracker.track(Keys.USER_LOGOUT, vEvent.properties);
        return vEvent.properties;
    }

    /**
     * This function is used send Screen View Details
     * @param screenName
     * @return 
     */
    @Override
    public Properties screenViewsTracking(String screenName) {
        try{
            VideoEvent vEvent = new VideoEvent();
            vEvent.setAppNameContext();
            tracker.screen("", screenName, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.BROWSER_LAUNCHED);
            if(url!=null){
                vEvent.data.putValue(Keys.TARGET_URL, url);
            }
            vEvent.setAppNameContext();

            tracker.track(Keys.BROWSER_LAUNCHED, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = getCommonPropertiesWithCurrentTime(currentTime,
                    videoId, Values.TRANSCRIPT_LANGUAGE);
            vEvent.properties.putValue(Keys.LANGUAGE, lang);
            vEvent.setContext(courseId, unitUrl,
                    Values.VIDEOPLAYER);

            tracker.track(Keys.LANGUAGE_CLICKED, vEvent.properties);
            return vEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;
    }

    @Override
    public Properties trackUserDoesNotHaveAccount() {
        try{
            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.USER_NO_ACCOUNT);
            vEvent.setAppNameContext();

            tracker.track(Keys.USER_NO_ACCOUNT, vEvent.properties);
            return vEvent.properties;
        }catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties trackUserFindsCourses() {
        try{
            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.USER_FIND_COURSES);
            vEvent.setAppNameContext();

            tracker.track(Keys.FIND_COURSES, vEvent.properties);
            return vEvent.properties;
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
            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.CONNECTION_CELL);
            vEvent.data.putValue(Keys.CELL_CARRIER, carrierName);
            vEvent.data.putValue(Keys.CELL_ZERO_RATED, isZeroRated);

            vEvent.setAppNameContext();
            tracker.track(Keys.TRACK_CELL_CONNECTION, vEvent.properties);
            return vEvent.properties;

        }catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties trackUserConnectionSpeed(String connectionType, float connectionSpeed) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.CONNECTION_SPEED);
            vEvent.data.putValue(Keys.CONNECTION_TYPE, connectionType);
            vEvent.data.putValue(Keys.CONNECTION_SPEED, connectionSpeed);

            vEvent.setAppNameContext();
            tracker.track(Keys.SPEED, vEvent.properties);
            return vEvent.properties;

        }catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties courseGroupAccessed(String courseName) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.ACCESS_COURSE_GROUP);

            vEvent.data.putValue(Keys.COURSE_ID, courseName);

            vEvent.setAppNameContext();
            tracker.track(Keys.COURSE_GROUP_ACCESSED, vEvent.properties);
            return vEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties gameGroupAccessed(long groupID, int groupUserCount) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.ACCESS_GAME_GROUP);

            vEvent.data.putValue(Keys.GROUP_ID, groupID);
            vEvent.data.putValue(Keys.GROUP_USER_COUNT, groupUserCount);

            vEvent.setAppNameContext();
            tracker.track(Keys.GAME_GROUP_ACCESSED, vEvent.properties);
            return vEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties groupCreated(long groupID, int invitedUserCount) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.CREATE_GAME_GROUP);

            vEvent.data.putValue(Keys.GROUP_ID, groupID);
            vEvent.data.putValue(Keys.GROUP_INVITED_USER_COUNT, invitedUserCount);

            vEvent.setAppNameContext();
            tracker.track(Keys.GAME_GROUP_CREATE, vEvent.properties);
            return vEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties groupInvited(long groupID, int invitedUserCount){

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.INVITE_GAME_GROUP);

            vEvent.data.putValue(Keys.GROUP_ID, groupID);
            vEvent.data.putValue(Keys.GROUP_INVITED_USER_COUNT, invitedUserCount);

            vEvent.setAppNameContext();
            tracker.track(Keys.GAME_GROUP_INVITE, vEvent.properties);
            return vEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties courseShared(String courseName, String socialNetwork) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.SOCIAL_COURSE_SHARED);

            vEvent.data.putValue(Keys.COURSE_ID, courseName);
            vEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

            vEvent.setAppNameContext();
            tracker.track(Keys.SOCIAL_COURSE_SHARED, vEvent.properties);
            return vEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;


    }

    @Override
    public Properties certificateShared(String courseName, String socialNetwork) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.SOCIAL_CERTIFICATE_SHARED);

            vEvent.data.putValue(Keys.COURSE_ID, courseName);
            vEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

            vEvent.setAppNameContext();
            tracker.track(Keys.SOCIAL_CERTIFICATE_SHARED, vEvent.properties);
            return vEvent.properties;


        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties socialConnectionEvent(boolean connected, String socialNetwork) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.SOCIAL_CONNECTION_CHANGE);

            vEvent.data.putValue(Keys.SOCIAL_CONNECTION_STATE, connected);
            vEvent.data.putValue(Keys.SOCIAL_NETWORK, socialNetwork);

            vEvent.setAppNameContext();
            tracker.track(Keys.SOCIAL_CONNECTION_CHANGE, vEvent.properties);
            return vEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

    @Override
    public Properties coursesVisibleToFriendsChange(boolean visible) {

        try {

            VideoEvent vEvent = new VideoEvent();
            vEvent.properties.putValue(Keys.NAME, Values.SETTING_COURSES_VISIBLE_CHANGE);

            vEvent.data.putValue(Keys.SETTING_COURSES_VISIBLE_STATE, visible);

            vEvent.setAppNameContext();
            tracker.track(Keys.SETTING_COURSES_VISIBLE_CHANGE, vEvent.properties);
            return vEvent.properties;

        } catch(Exception e){
            logger.error(e);
        }
        return null;

    }

}
