package org.edx.mobile.module.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.edx.mobile.util.images.ShareUtils;

import java.util.Map;

/**
 * An interface that defines methods for all analytics events, to be implemented for all analytics
 * services that are used in the app. This class contains all the {@link Screens} & {@link Events}.
 * Additionally all the {@link Keys} and their corresponding {@link Values} are defined to make
 * Screens and Events more meaningful.
 */
public interface Analytics {

    /**
     * This function is used to send the screen tracking event, with an extra event for
     * sending course id.
     *
     * @param screenName The screen name to track
     * @param courseId   course id of the course we are viewing
     * @param action     any custom action we need to send with event
     * @param values     any custom key- value pairs we need to send with event
     */
    void trackScreenView(@NonNull String screenName, @Nullable String courseId,
                         @Nullable String action, @Nullable Map<String, String> values);

    /**
     * This function is used to track Video Playing
     *
     * @param videoId     - Video Id that is being Played
     * @param currentTime - Video Playing started at
     * @param unitUrl     - Page Url for that Video
     * @param courseId    - CourseId under which the video is present
     */
    void trackVideoPlaying(String videoId, Double currentTime,
                           String courseId, String unitUrl);

    /**
     * This function is used to track Video Pause
     *
     * @param videoId     - Video Id that is being Played
     * @param currentTime - Video Playing started at
     * @param courseId    - CourseId under which the video is present
     * @param unitUrl     - Page Url for that Video
     */
    void trackVideoPause(String videoId, Double currentTime,
                         String courseId, String unitUrl);

    /**
     * This function is used to track Video Stop
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     */
    void trackVideoStop(String videoId, Double currentTime,
                        String courseId, String unitUrl);

    /**
     * This function is used to Show Transcript
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     */
    void trackShowTranscript(String videoId, Double currentTime,
                             String courseId, String unitUrl);

    /**
     * This function is used to Hide Transcript
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     */
    void trackHideTranscript(String videoId, Double currentTime,
                             String courseId, String unitUrl);

    /**
     * This function is used to track Video Loading
     *
     * @param videoId
     * @param courseId
     * @param unitUrl
     */
    void trackVideoLoading(String videoId, String courseId, String unitUrl);

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
    void trackVideoSeek(String videoId, Double oldTime, Double newTime,
                        String courseId, String unitUrl, Boolean skipSeek);

    /* Events not mentioned in PDF */

    /**
     * This function is used to track Video Download completed
     *
     * @param videoId  - Video id for which download has started
     * @param courseId
     * @param unitUrl
     */
    void trackDownloadComplete(String videoId, String courseId,
                               String unitUrl);

    /**
     * This function is used to track launching the browser
     *
     * @param url
     */
    void trackBrowserLaunched(String url);

    /**
     * This function is used to track Bulk Download from Subsection
     *
     * @param section      - Section in which the subsection is present
     * @param subSection   - Subsection from which the download started
     * @param enrollmentId - Course under which the subsection is present
     * @param videoCount   - no of videos started downloading
     */
    void trackSubSectionBulkVideoDownload(String section, String subSection,
                                          String enrollmentId, long videoCount);

    /**
     * This function is used to track User Login
     *
     * @param method - will take the following inputs “Password”|”Google”|”Facebook”
     */
    void trackUserLogin(String method);

    /**
     * This function is used to track user logout
     */
    void trackUserLogout();

    /**
     * This function is used to track Language changed for Transcripts
     *
     * @param videoId
     * @param currentTime
     * @param lang
     * @param courseId
     * @param unitUrl
     */
    void trackTranscriptLanguage(String videoId, Double currentTime,
                                 String lang, String courseId, String unitUrl);

    /**
     * This function is used to track Video Download started from Video List
     *
     * @param videoId  -  Video id for which download has started
     * @param courseId
     * @param unitUrl
     */
    void trackSingleVideoDownload(String videoId, String courseId,
                                  String unitUrl);

    /**
     * This function is used to track Video Orientation
     *
     * @param videoId
     * @param currentTime
     * @param isLandscape -  true / false based on orientation
     * @param courseId
     * @param unitUrl
     */
    void trackVideoOrientation(String videoId, Double currentTime,
                               boolean isLandscape, String courseId, String unitUrl);

    void trackDiscoverCoursesClicked();

    void trackExploreSubjectsClicked();

    /**
     * This function is used to track if user clicks on Sign up on landing page
     */
    void trackUserSignUpForAccount();

    /**
     * This function is used to track if user clicks on Find Courses
     */
    void trackUserFindsCourses();

    /**
     * This function is used to track if user clicks on Create Account on registration screen
     */
    void trackCreateAccountClicked(String appVersion, String source);

    /**
     * This function is used to track if user clicks on Enroll in the FindCourses Activity
     *
     * @param courseId   - Course Id for which user selected enroll
     * @param emailOptIn - Flag to show user wants to opt in for email notification
     */
    void trackEnrollClicked(String courseId, boolean emailOptIn);

    void trackNotificationReceived(@Nullable String courseId);

    void trackNotificationTapped(@Nullable String courseId);

    void trackUserConnectionSpeed(String connectionType, float connectionSpeed);

    void certificateShared(String courseId, String certificateUrl, ShareUtils.ShareType method);

    void courseDetailShared(String courseId, String aboutUrl, ShareUtils.ShareType method);

    void trackCourseComponentViewed(String blockId, String courseId, String minifiedBlockId);

    void trackOpenInBrowser(String blockId, String courseId, boolean isSupported, String minifiedBlockId);

    void trackProfileViewed(@NonNull String username);

    void trackProfilePhotoSet(boolean fromCamera);

    void identifyUser(String userID, String email, String username);

    /**
     * This resets the Identify user once the user has logged out
     */
    void resetIdentifyUser();

    /**
     * This function is used to track if user views the App Rating view.
     *
     * @param versionName Version name of app.
     */
    void trackAppRatingDialogViewed(String versionName);

    /**
     * This function is used to track if user cancels the App Rating view.
     *
     * @param versionName Version name of app.
     */
    void trackAppRatingDialogCancelled(String versionName);

    /**
     * This function is used to track if user submits rating on the App Rating view.
     *
     * @param versionName Version name of app.
     * @param rating Rating given by user.
     */
    void trackUserSubmitRating(String versionName, int rating);

    /**
     * This function is used to track if user selects Send Feedback after rating the app.
     *
     * @param versionName Version name of app.
     * @param rating Rating given by user.
     */
    void trackUserSendFeedback(String versionName, int rating);

    /**
     * This function is used to track if user rates the app and then selects Maybe Later,
     * could be either from Feedback dialog or Rate The App dialog.
     *
     * @param versionName Version name of app.
     * @param rating Rating given by user.
     */
    void trackUserMayReviewLater(String versionName, int rating);

    /**
     * This function is used to track if user gives positive rating and selects Rate The App option.
     *
     * @param versionName Version name of app.
     * @param rating Rating given by user.
     */
    void trackRateTheAppClicked(String versionName, int rating);

    /**
     * This function is used to track if user presses the cross button on WhatsNew screen.
     *
     * @param versionName     Version name of app.
     * @param totalViewed     The total number of screens a user viewed.
     * @param currentlyViewed The screen being currently viewed.
     * @param totalScreens    Total number of screens.
     */
    void trackWhatsNewClosed(@NonNull String versionName, int totalViewed, int currentlyViewed, int totalScreens);

    /**
     * This function is used to track if user presses the done button on WhatsNew screen.
     *
     * @param versionName  Version name of app.
     * @param totalScreens Total number of screens.
     */
    void trackWhatsNewSeen(@NonNull String versionName, int totalScreens);

    /**
     * Track deletion of all videos within a subsection.
     *
     * @param courseId     ID of the course.
     * @param subsectionId ID of the subsection.
     */
    void trackSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId);

    /**
     * Track cancelling of all videos' deletion within a subsection.
     *
     * @param courseId     ID of the course.
     * @param subsectionId ID of the subsection.
     */
    void trackUndoingSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId);

    /**
     * Track deletion of a video unit.
     *
     * @param courseId ID of the course.
     * @param unitId   ID of the unit.
     */
    void trackUnitVideoDelete(@NonNull String courseId, @NonNull String unitId);

    /**
     * Track cancelling of a video unit's deletion.
     *
     * @param courseId ID of the course.
     * @param unitId   ID of the unit.
     */
    void trackUndoingUnitVideoDelete(@NonNull String courseId, @NonNull String unitId);

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
        String SUBSECTION_ID = "subsection_id";
        String UNIT_ID = "unit_id";
        String SUPPORTED = "supported";
        String DEVICE_ORIENTATION = "device-orientation";

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
        String AUTHOR = "author";

        String COMPONENT_VIEWED = "Component Viewed";

        String APP_VERSION = "app_version";
        String RATING = "rating";
        // WhatsNew keys
        String TOTAL_VIEWED = "total_viewed";
        String CURRENTLY_VIEWED = "currently_viewed";
        String TOTAL_SCREENS = "total_screens";
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
        String PROFILE_VIEWED = "edx.bi.app.profile.view";
        String PROFILE_PHOTO_SET = "edx.bi.app.profile.setphoto";
        String COMPONENT_VIEWED = "edx.bi.app.navigation.component.viewed";
        String OPEN_IN_BROWSER = "edx.bi.app.navigation.open-in-browser";
        String OPEN_IN_WEB_SUPPORTED = "Open in browser - Supported";
        String OPEN_IN_WEB_NOT_SUPPORTED = "Open in browser - Unsupported";
        String LANDSCAPE = "landscape";
        String PORTRAIT = "portrait";

        String WIFI = "wifi";
        String CELL_DATA = "cell_data";
        String POSTS_ALL = "all_posts";
        String POSTS_FOLLOWING = "posts_following";
        // App review event values
        String APP_REVIEWS_CATEGORY = "app-reviews";
        String APP_REVIEWS_VIEW_RATING = "edx.bi.app.app_reviews.view_rating";
        String APP_REVIEWS_DISMISS_RATING = "edx.bi.app.app_reviews.dismiss_rating";
        String APP_REVIEWS_SUBMIT_RATING = "edx.bi.app.app_reviews.submit_rating";
        String APP_REVIEWS_SEND_FEEDBACK = "edx.bi.app.app_reviews.send_feedback";
        String APP_REVIEWS_MAYBE_LATER = "edx.bi.app.app_reviews.maybe_later";
        String APP_REVIEWS_RATE_THE_APP = "edx.bi.app.app_reviews.rate_the_app";
        // WhatsNew event values
        String WHATS_NEW_CATEGORY = "whats-new";
        String WHATS_NEW_CLOSE = "edx.bi.app.whats_new.close";
        String WHATS_NEW_DONE = "edx.bi.app.whats_new.done";
        // Course Videos event values
        String VIDEOS_SUBSECTION_DELETE = "edx.bi.app.video.delete.subsection";
        String VIDEOS_UNDO_SUBSECTION_DELETE = "edx.bi.app.video.undo.subsection.delete";
        String VIDEOS_UNIT_DELETE = "edx.bi.app.video.delete.unit";
        String VIDEOS_UNDO_UNIT_DELETE = "edx.bi.app.video.undo.unit.delete";
    }

    interface Screens {
        String COURSE_INFO_SCREEN = "Course Info";
        String LAUNCH_ACTIVITY = "Launch";
        String REGISTER = "Register";
        String LOGIN = "Login";
        String COURSE_DASHBOARD = "Course Dashboard";
        String COURSE_OUTLINE = "Course Outline";
        String COURSE_HANDOUTS = "Course Handouts";
        String COURSE_ANNOUNCEMENTS = "Course Announcements";
        String COURSE_DATES = "Course Dates";
        String SECTION_OUTLINE = "Section Outline";
        String UNIT_DETAIL = "Unit Detail";
        String CERTIFICATE = "View Certificate";
        String DOWNLOADS = "Downloads";
        String FIND_COURSES = "Find Courses";
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
        String PROFILE_VIEW = "Profile View";
        String PROFILE_EDIT = "Profile Edit";
        String PROFILE_CROP_PHOTO = "Crop Photo";
        String PROFILE_CHOOSE_BIRTH_YEAR = "Choose Form Value Birth year";
        String PROFILE_CHOOSE_LOCATION = "Choose Form Value Location";
        String PROFILE_CHOOSE_LANGUAGE = "Choose Form Value Primary language";
        String PROFILE_EDIT_TEXT_VALUE = "Edit Text Form Value";
        String APP_REVIEWS_VIEW_RATING = "AppReviews: View Rating";
        String WHATS_NEW = "WhatsNew: Whats New";
        String VIDEOS_COURSE_VIDEOS = "Videos: Course Videos";
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
        String COMPONENT_VIEWED = "Component Viewed";
        String OPEN_IN_BROWSER = "Browser Launched";
        String PUSH_NOTIFICATION_RECEIVED = "notification-received";
        String PUSH_NOTIFICATION_TAPPED = "notification-tapped";
        String PROFILE_VIEWED = "Viewed a profile";
        String PROFILE_PHOTO_SET = "Set a profile picture";
        // App review events
        String APP_REVIEWS_VIEW_RATING = "AppReviews: View Rating";
        String APP_REVIEWS_DISMISS_RATING = "AppReviews: Dismiss Rating";
        String APP_REVIEWS_SUBMIT_RATING = "AppReviews: Submit Rating";
        String APP_REVIEWS_SEND_FEEDBACK = "AppReviews: Send Feedback";
        String APP_REVIEWS_MAYBE_LATER = "AppReviews: Maybe Later";
        String APP_REVIEWS_RATE_THE_APP = "AppReviews: Rate The App";
        // WhatsNew events
        String WHATS_NEW_CLOSE = "WhatsNew: Close";
        String WHATS_NEW_DONE = "WhatsNew: Done";
        // Course Videos events
        String VIDEOS_SUBSECTION_DELETE = "Videos: Subsection Delete";
        String VIDEOS_UNDO_SUBSECTION_DELETE = "Videos: Undo Subsection Delete";
        String VIDEOS_UNIT_DELETE = "Videos: Unit Delete";
        String VIDEOS_UNDO_UNIT_DELETE = "Videos: Undo Unit Delete";
    }

    /**
     * We can't have concrete functions inside interfaces till Java 8, therefore this
     * class has been defined to add static utilities to this interface.
     */
    class Util {
        /**
         * Resolves and returns the string alternative of the given share type.
         *
         * @param shareType The share type.
         * @return The string alternative of the given share type.
         */
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

    /**
     * Defines the analytics events that need to be fired.
     */
    interface OnEventListener {
        /**
         * Fires a screen event.
         */
        void fireScreenEvent();
    }
}
