package org.edx.mobile.module.analytics;

import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

public interface ISegment {

    /*
     * Events mentioned in PDF 
     * 1)Identify 
     * 2)Video playing events
     * a)edx.video.played 
     * b)edx.video.paused
     * c)edx.video.stopped
     * d)edx.video.transcript.shown 
     * e)edx.video.transcript.hidden
     * 3)Load events edx.video.loaded
     * 4)Seek events (NOTE: may not be implemented for General
     * Availability due to technical limitations in the video player)
     */

    Traits identifyUser(String userID, String email, String username);

    Properties trackVideoPlaying(String videoId, Double currentTime,
            String courseId, String unitUrl);

    Properties trackVideoPause(String videoId, Double currentTime,
            String courseId, String unitUrl);

    Properties trackVideoStop(String videoId, Double currentTime,
            String courseId, String unitUrl);

    Properties trackShowTranscript(String videoId, Double currentTime,
            String courseId, String unitUrl);

    Properties trackHideTranscript(String videoId, Double currentTime,
            String courseId, String unitUrl);

    Properties trackVideoLoading(String videoId, String courseId, String unitUrl);

    Properties trackVideoSeek(String videoId, Double oldTime,
            Double newTime, String courseId, String unitUrl);

    void resetIdentifyUser();

    /* Events not mentioned in PDF */

    Properties screenViewsTracking(String screenName);

    Properties trackDownloadComplete(String videoId, String courseId,
            String unitUrl);

    Properties trackOpenInBrowser(String url);

    Properties trackSectionBulkVideoDownload(String enrollmentId,
            String section, long videoCount);

    Properties trackSubSectionBulkVideoDownload(String section,
            String subSection, String enrollmentId, long videoCount);

    Properties trackUserLogin(String method);
    
    Properties trackUserLogout();

    Properties trackTranscriptLanguage(String videoId, Double currentTime,
            String lang, String courseId, String unitUrl);

    Properties trackSingleVideoDownload(String videoId, String courseId,
            String unitUrl);

    Properties trackVideoOrientation(String videoId, Double currentTime,
            boolean isLandscape, String courseId, String unitUrl);
    
    Properties trackUserDoesNotHaveAccount();
    
    Properties trackUserFindsCourses();

    /**
     * Sets given tracker instance and uses it for analytics.
     * This method is useful in some cases where a given tracker is to be used.
     * For example, unit tests might use mocked tracker object.
     * @param tracker
     */
    void setTracker(SegmentTracker tracker);
    
    public static interface Keys{
        public static final String NAME = "name";
        public static final String OLD_TIME = "old_time";
        public static final String NEW_TIME = "new_time";
        public static final String SEEK_TYPE = "seek_type";
        public static final String REQUESTED_SKIP_INTERVAL = "requested_skip_interval";
        public static final String MODULE_ID = "module_id";
        public static final String CODE = "code";
        public static final String CURRENT_TIME = "current_time";
        public static final String COURSE_ID = "course_id";
        public static final String OPEN_BROWSER = "open_in_browser_url";
        public static final String COMPONENT = "component";
        public static final String COURSE_SECTION = "course_section";
        public static final String COURSE_SUBSECTION = "course_subsection";
        public static final String NO_OF_VIDEOS = "number_of_videos";
        public static final String FULLSCREEN = "settings.video.fullscreen";
        public static final String LANGUAGE = "language";
        public static final String TARGET_URL = "target_url";
        public static final String CONTEXT = "context";
        public static final String DATA = "data";
        public static final String METHOD = "method";
        public static final String APP = "app_name";
        public static final String LOADED_VIDEO = "Loaded Video";
        public static final String PLAYED_VIDEO = "Played Video";
        public static final String PAUSED_VIDEO = "Paused Video";
        public static final String STOPPED_VIDEO = "Stopped Video";
        public static final String SEEK_VIDEO = "Seeked Video";
        public static final String SHOW_TRANSCRIPT = "Show Transcript";
        public static final String HIDE_TRANSCRIPT = "Hide Transcript";
        public static final String VIDEO_DOWNLOADED = "Video Downloaded";
        public static final String BULK_DOWNLOAD_SECTION = "Bulk Download Section";
        public static final String BULK_DOWNLOAD_SUBSECTION = "Bulk Download Subsection";
        public static final String SINGLE_VIDEO_DOWNLOAD = "Single Video Download";
        public static final String SCREEN_TOGGLED = "Screen Toggled";
        public static final String USER_LOGIN = "User Login";
        public static final String USER_LOGOUT = "User Logout";
        public static final String BROWSER_LAUNCHED = "Browser Launched";
        public static final String LANGUAGE_CLICKED = "Language Clicked";
        public static final String USER_NO_ACCOUNT = "User Has No Account Clicked";
        public static final String FIND_COURSES = "Find Courses Clicked";
    }
    
    public static interface Values{
        public static final String SKIP = "skip";
        public static final String MOBILE = "mobile";
        public static final String VIDEOPLAYER = "videoplayer";
        public static final String PASSWORD = "Password";
        public static final String FACEBOOK = "Google";
        public static final String GOOGLE = "Facebook";
        public static final String DOWNLOAD_MODULE = "downloadmodule";
        public static final String VIDEO_LOADED = "edx.video.loaded";
        public static final String VIDEO_PLAYED = "edx.video.played";
        public static final String VIDEO_PAUSED = "edx.video.paused";
        public static final String VIDEO_STOPPED = "edx.video.stopped";
        //The seek event name has been changed as per MOB-1273
        public static final String VIDEO_SEEKED = "edx.video.position.changed";
        public static final String TRANSCRIPT_SHOWN = "edx.video.transcript.shown";
        public static final String TRANSCRIPT_HIDDEN = "edx.video.transcript.hidden";
        public static final String TRANSCRIPT_LANGUAGE = "edx.bi.video.transcript.language.selected";
        public static final String FULLSREEN_TOGGLED = "edx.bi.video.screen.fullscreen.toggled";
        public static final String BROWSER_LAUNCHED = "edx.bi.app.browser.launched";
        public static final String SINGLE_VIDEO_DOWNLOAD = "edx.bi.video.download.requested";
        public static final String BULKDOWNLOAD_SECTION = "edx.bi.video.section.bulkdownload.requested";
        public static final String BULK_DOWNLOAD_SUBSECTION = "edx.bi.video.subsection.bulkdownload.requested";
        public static final String VIDEO_DOWNLOADED = "edx.bi.video.downloaded";
        public static final String USERLOGOUT = "edx.bi.app.user.logout";
        public static final String USERLOGIN = "edx.bi.app.user.login";
        public static final String APP_NAME = "edx.mobileapp.android";
        public static final String USER_NO_ACCOUNT = "edx.bi.app.user.no_account";
        public static final String USER_FIND_COURSES = "edx.bi.app.find_courses";
        public static final String COURSE_INFO_SCREEN = "Course Info";
    }
}
