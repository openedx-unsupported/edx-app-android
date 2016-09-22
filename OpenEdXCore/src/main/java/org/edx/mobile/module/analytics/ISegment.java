package org.edx.mobile.module.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.util.images.ShareUtils;

import java.util.Map;

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
            Double newTime, String courseId, String unitUrl, Boolean skipSeek);

    void resetIdentifyUser();

    /* Events not mentioned in PDF */

    Properties trackScreenView(@NonNull String screenName);

    Properties trackScreenView(@NonNull String screenName, @Nullable String courseId,
            @Nullable String value);

    Properties trackScreenView(@NonNull String screenName, @Nullable String courseId,
            @Nullable String value, @Nullable Map<String, String> values);

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

    Properties trackDiscoverCoursesClicked();

    Properties trackExploreSubjectsClicked();

    Properties trackUserSignUpForAccount();

    Properties trackUserFindsCourses();

    Properties trackCreateAccountClicked(String appVersion, String source);

    Properties trackEnrollClicked(String courseId, boolean email_opt_in);

    Properties trackNotificationReceived(@Nullable String courseId);

    Properties trackNotificationTapped(@Nullable String courseId);


    /**
     * Sets given tracker instance and uses it for analytics.
     * This method is useful in some cases where a given tracker is to be used.
     * For example, unit tests might use mocked tracker object.
     * @param tracker
     */
    void setTracker(ISegmentTracker tracker);

    Properties trackUserConnectionSpeed(String connectionType, float connectionSpeed);

    Properties certificateShared(@NonNull String courseId, @NonNull String certificateUrl, @NonNull ShareUtils.ShareType shareType);

    Properties courseDetailShared(@NonNull String courseId, @NonNull String aboutUrl, @NonNull ShareUtils.ShareType shareType);

    Properties trackCourseOutlineMode(boolean isVideoMode);

    Properties trackCourseComponentViewed(String blockId, String courseId);

    Properties trackOpenInBrowser(String blockId, String courseId, boolean isSupported);

    Properties trackProfileViewed(@NonNull String username);

    Properties trackProfilePhotoSet(boolean fromCamera);


    interface Keys {
        String NAME = "name";
        String OLD_TIME = "old_time";
        String NEW_TIME = "new_time";
        String SEEK_TYPE = "seek_type";
        String REQUESTED_SKIP_INTERVAL = "requested_skip_interval";
        String MODULE_ID = "module_id";
        String CODE = "code";
        String CURRENT_TIME = "current_time";
        String COURSE_ID = "course_id";
        String OPEN_BROWSER = "open_in_browser_url";
        String COMPONENT = "component";
        String COURSE_SECTION = "course_section";
        String COURSE_SUBSECTION = "course_subsection";
        String NO_OF_VIDEOS = "number_of_videos";
        String FULLSCREEN = "settings.video.fullscreen";
        String LANGUAGE = "language";
        String TARGET_URL = "target_url";
        String URL = "url";
        String CONTEXT = "context";
        String DATA = "data";
        String METHOD = "method";
        String APP = "app_name";
        String EMAIL_OPT_IN = "email_opt_in";
        String PROVIDER = "provider";
        String BLOCK_ID = "block_id";
        String SUPPORTED = "supported";
        String NEW_OUTLINE_MODE = "new-mode";
        String DEVICE_ORIENTATION = "device-orientation";
        String NAVIGATION_MODE = "navigation-mode";

        String CELL_CARRIER = "cell_carrier";
        String CELL_ZERO_RATED = "cell_zero_rated";

        String CONNECTION_TYPE = "connection_type";
        String CONNECTION_SPEED = "connection_speed";

        String TYPE = "type";
        String CATEGORY = "category";
        String LABEL = "label";
        String ACTION = "action";
        String SEARCH_STRING = "search_string";
        String TOPIC_ID = "topic_id";
        String THREAD_ID = "thread_id";
        String RESPONSE_ID = "response_id";

        String COMPONENT_VIEWED = "Component Viewed";
    }

    interface Values {
        String SCREEN = "screen";
        String SKIP = "skip";
        String SLIDE = "slide";
        String MOBILE = "mobile";
        String VIDEOPLAYER = "videoplayer";
        String PASSWORD = "Password";
        String FACEBOOK = "Google";
        String GOOGLE = "Facebook";
        String DOWNLOAD_MODULE = "downloadmodule";
        String VIDEO_LOADED = "edx.video.loaded";
        String VIDEO_PLAYED = "edx.video.played";
        String VIDEO_PAUSED = "edx.video.paused";
        String VIDEO_STOPPED = "edx.video.stopped";
        //The seek event name has been changed as per MOB-1273
        String VIDEO_SEEKED = "edx.video.position.changed";
        String TRANSCRIPT_SHOWN = "edx.video.transcript.shown";
        String TRANSCRIPT_HIDDEN = "edx.video.transcript.hidden";
        String TRANSCRIPT_LANGUAGE = "edx.bi.video.transcript.language.selected";
        String FULLSREEN_TOGGLED = "edx.bi.video.screen.fullscreen.toggled";
        String BROWSER_LAUNCHED = "edx.bi.app.browser.launched";
        String SINGLE_VIDEO_DOWNLOAD = "edx.bi.video.download.requested";
        String BULKDOWNLOAD_SECTION = "edx.bi.video.section.bulkdownload.requested";
        String BULK_DOWNLOAD_SUBSECTION = "edx.bi.video.subsection.bulkdownload.requested";
        String VIDEO_DOWNLOADED = "edx.bi.video.downloaded";
        String USERLOGOUT = "edx.bi.app.user.logout";
        String USERLOGIN = "edx.bi.app.user.login";
        String APP_NAME = "edx.mobileapp.android";
        String DISCOVER_COURSES_CLICK = "edx.bi.app.discover.courses.tapped";
        String EXPLORE_SUBJECTS_CLICK = "edx.bi.app.discover.explore.tapped";
        String USER_FIND_COURSES = "edx.bi.app.search.find_courses.clicked";
        String CREATE_ACCOUNT_CLICK = "edx.bi.app.user.register.clicked";
        String USER_COURSE_ENROLL = "edx.bi.app.course.enroll.clicked";
        String USER_NO_ACCOUNT = "edx.bi.app.user.signup.clicked";
        String CONVERSION = "conversion";
        String USER_ENGAGEMENT = "user-engagement";
        String COURSE_DISCOVERY = "course-discovery";

        String PUSH_NOTIFICATION = "notifications";
        String ANNOUNCEMENT = "announcement";

        String CONNECTION_CELL = "edx.bi.app.connection.cell";
        String CONNECTION_SPEED = "edx.bi.app.connection.speed";

        String NOTIFICATION_RECEIVED = "edx.bi.app.notification.course.update.received";
        String NOTIFICATION_TAPPED = "edx.bi.app.notification.course.update.tapped";

        String SOCIAL_CERTIFICATE_SHARED = "edx.bi.app.certificate.shared";
        String SOCIAL_COURSE_DETAIL_SHARED = "edx.bi.app.course.shared";

        String NAVIGATION = "navigation";
        String SOCIAL_SHARING = "social-sharing";
        String PROFILE = "profiles";
        String CAMERA = "camera";
        String LIBRARY = "library";
        String SWITCH_OUTLINE_MODE = "edx.bi.app.navigation.switched-mode.clicked";
        String PROFILE_VIEWED = "edx.bi.app.profile.view";
        String PROFILE_PHOTO_SET = "edx.bi.app.profile.setphoto";
        String COMPONENT_VIEWED = "edx.bi.app.navigation.component.viewed";
        String OPEN_IN_BROWSER = "edx.bi.app.navigation.open-in-browser";
        String OUTLINE_MODE_FULL = "full";
        String OUTLINE_MODE_VIDEO = "video";
        String SWITCH_TO_FULL_MODE = "Switch to Full Mode";
        String SWITCH_TO_VIDEO_MODE = "Switch to Video Mode";
        String OPEN_IN_WEB_SUPPORTED = "Open in browser - Supported";
        String OPEN_IN_WEB_NOT_SUPPORTED = "Open in browser - Unsupported";
        String LANDSCAPE = "landscape";
        String PORTRAIT = "portrait";

        String WIFI = "wifi";
        String CELL_DATA = "cell_data";
        String POSTS_ALL = "all_posts";
        String POSTS_FOLLOWING = "posts_following";
    }

    interface Screens {
        String COURSE_INFO_SCREEN = "Course Info";
        String LAUNCH_ACTIVITY = "Launch";
        String COURSE_DASHBOARD = "Course Dashboard";
        String COURSE_OUTLINE = "Course Outline";
        String SECTION_OUTLINE = "Section Outline";
        String UNIT_DETAIL = "Unit Detail";
        String CERTIFICATE = "Certificate";
        String DOWNLOADS = "Downloads";
        String FIND_COURSES = "Find Courses";
        String LOGIN = "Login";
        String MY_VIDEOS = "My Videos";
        String MY_VIDEOS_ALL = "My Videos - All Videos";
        String MY_VIDEOS_RECENT = "My Videos - Recent Videos";
        String MY_COURSES = "My Courses";
        String SETTINGS = "Settings";

        String FORUM_VIEW_TOPICS = "Forum: View Topics";
        String FORUM_SEARCH_THREADS = "Forum: Search Threads";
        String FORUM_VIEW_TOPIC_THREADS = "Forum: View Topic Threads";
        String FORUM_CREATE_TOPIC_THREAD = "Forum: Create Topic Thread";
        String FORUM_VIEW_THREAD = "Forum: View Thread";
        String FORUM_ADD_RESPONSE = "Forum: Add Thread Response";
        String FORUM_VIEW_RESPONSE_COMMENTS = "Forum: View Response Comments";
        String FORUM_ADD_RESPONSE_COMMENT = "Forum: Add Response Comment";
    }

    interface Events {
        String LOADED_VIDEO = "Loaded Video";
        String PLAYED_VIDEO = "Played Video";
        String PAUSED_VIDEO = "Paused Video";
        String STOPPED_VIDEO = "Stopped Video";
        String SEEK_VIDEO = "Seeked Video";
        String SHOW_TRANSCRIPT = "Show Transcript";
        String HIDE_TRANSCRIPT = "Hide Transcript";
        String VIDEO_DOWNLOADED = "Video Downloaded";
        String BULK_DOWNLOAD_SECTION = "Bulk Download Section";
        String BULK_DOWNLOAD_SUBSECTION = "Bulk Download Subsection";
        String SINGLE_VIDEO_DOWNLOAD = "Single Video Download";
        String SCREEN_TOGGLED = "Screen Toggled";
        String USER_LOGIN = "User Login";
        String USER_LOGOUT = "User Logout";
        String BROWSER_LAUNCHED = "Browser Launched";
        String LANGUAGE_CLICKED = "Language Clicked";
        String SIGN_UP = "Sign up Clicked";
        String FIND_COURSES = "Find Courses Clicked";
        String CREATE_ACCOUNT_CLICKED = "Create Account Clicked";
        String ENROLL_COURSES = "Enroll Course Clicked";
        String DISCOVER_COURSES = "Discover Courses";
        String EXPLORE_SUBJECTS = "Explore Subjects";
        String SPEED = "Connected Speed Report";
        String SOCIAL_CERTIFICATE_SHARED = "Shared a certificate";
        String SOCIAL_COURSE_DETAIL_SHARED = "Shared a course";
        String SWITCH_OUTLINE_MODE = "Switch outline mode";
        String COMPONENT_VIEWED = "Component Viewed";
        String OPEN_IN_BROWSER = "Browser Launched";
        String PUSH_NOTIFICATION_RECEIVED = "notification-received";
        String PUSH_NOTIFICATION_TAPPED = "notification-tapped";
        String PROFILE_VIEWED = "Viewed a profile";
        String PROFILE_PHOTO_SET = "Set a profile picture";
    }
}
