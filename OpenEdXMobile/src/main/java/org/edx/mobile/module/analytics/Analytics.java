package org.edx.mobile.module.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.segment.analytics.Properties;

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
     * @param courseId    - CourseId under which the video is present
     * @param unitUrl     - Page Url for that Video
     * @param playMedium  - Play Medium (e.g {@link Values#GOOGLE_CAST})
     */
    void trackVideoPlaying(String videoId, Double currentTime,
                           String courseId, String unitUrl, String playMedium);

    /**
     * This function is used to track Video Pause
     *
     * @param videoId     - Video Id that is being Played
     * @param currentTime - Video Playing started at
     * @param courseId    - CourseId under which the video is present
     * @param unitUrl     - Page Url for that Video
     * @param playMedium  - Play Medium (e.g {@link Values#YOUTUBE})
     */
    void trackVideoPause(String videoId, Double currentTime,
                         String courseId, String unitUrl, String playMedium);

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
     * This function is used to track the video playback speed changes
     *
     * @param videoId
     * @param currentTime
     * @param courseId
     * @param unitUrl
     * @param oldSpeed
     * @param newSpeed
     */
    void trackVideoSpeed(String videoId, Double currentTime, String courseId,
                         String unitUrl, float oldSpeed, float newSpeed);

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
     * Track user's successful course upgrade.
     *
     * @param blockId         ID of the locked course unit from which the user is redirected to upgrade screen.
     * @param courseId        ID of the course which user has upgraded.
     * @param minifiedBlockId Block ID of the locked course unit from which the user is redirected to upgrade screen.
     */
    void trackCourseUpgradeSuccess(String blockId, String courseId, String minifiedBlockId);

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
     * @param playMedium  - Play Medium (e.g {@link Values#YOUTUBE})
     */
    void trackVideoOrientation(String videoId, Double currentTime,
                               boolean isLandscape, String courseId, String unitUrl, String playMedium);

    /**
     * Tracks search of courses in the app.
     *
     * @param searchQuery The search query.
     * @param isLoggedIn  <code>true</code> if the user is logged-in, <code>false</code> otherwise.
     * @param versionName App's version.
     */
    void trackCoursesSearch(String searchQuery, boolean isLoggedIn, String versionName);

    /**
     * This function is used to track if user clicks on Sign up on landing page
     */
    void trackUserSignUpForAccount();

    /**
     * This function is used to track if user clicks on Find Courses
     */
    void trackUserFindsCourses();

    /**
     * Track if user clicks on Create Account button on Registration screen.
     *
     * @param appVersion Version of app.
     * @param source     Source through which the user is going to register.
     */
    void trackCreateAccountClicked(@NonNull String appVersion, @Nullable String source);

    /**
     * Track successful registration of a user.
     *
     * @param appVersion Version of app.
     * @param source     Source through which the user has completed registration.
     */
    void trackRegistrationSuccess(@NonNull String appVersion, @Nullable String source);

    /**
     * Track user's click on the Enroll button of Find Courses Detail screen.
     *
     * @param courseId   ID of the course for which user is going to enroll.
     * @param emailOptIn Flag to represent if user wants to opt in for email notification.
     */
    void trackEnrollClicked(@NonNull String courseId, boolean emailOptIn);

    /**
     * Track user's successful enrollment in a course.
     *
     * @param courseId   ID of the course which user has enrolled in.
     * @param emailOptIn Flag to represent if user wants to opt in for email notification.
     */
    void trackEnrolmentSuccess(@NonNull String courseId, boolean emailOptIn);

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

    /**
     * Track the pressing of bulk download switch to ON state.
     *
     * @param courseId                    ID of the course.
     * @param totalDownloadableVideos     Number of videos that can be downloaded in a course.
     * @param remainingDownloadableVideos Remaining videos that can be downloaded within a course.
     */
    void trackBulkDownloadSwitchOn(@NonNull String courseId, int totalDownloadableVideos,
                                   int remainingDownloadableVideos);

    /**
     * Track the pressing of bulk download switch to OFF state.
     *
     * @param courseId                ID of the course.
     * @param totalDownloadableVideos Number of videos that can be downloaded in a course.
     */
    void trackBulkDownloadSwitchOff(@NonNull String courseId, int totalDownloadableVideos);

    /**
     * Tracks the pressing of a subject item.
     *
     * @param subjectId ID of the subject.
     */
    void trackSubjectClicked(@NonNull String subjectId);

    /**
     * Track the pressing of 'Download Video to SD Card' switch to ON state.
     */
    void trackDownloadToSdCardSwitchOn();

    /**
     * Track the pressing of 'Download Video to SD Card' switch to OFF state.
     */
    void trackDownloadToSdCardSwitchOff();

    /**
     * Track the parameters relevant to the experiment of Firebase Remote Configs.
     * Ref: https://openedx.atlassian.net/browse/LEARNER-7394
     *
     * @param experimentName
     * @param values any custom key-value pairs we need to send with event
     */
    void trackExperimentParams(String experimentName, Map<String, String> values);

    /**
     * Track the casting device connection.
     *
     * @param eventName       Cast Event Name
     * @param connectionState State of casting device (e.g {@link Values#CAST_CONNECTED})
     * @param playMedium      Casting device playMedium (e.g {@link Values#GOOGLE_CAST})
     */
    void trackCastDeviceConnectionChanged(@NonNull String eventName, @NonNull String connectionState,
                                          @NonNull String playMedium);

    /**
     * Track the PLS Course Dates Banner appearance
     *
     * @param biValue        biValue of course date banner type
     * @param courseId       course id of the course where banner appears
     * @param enrollmentMode enrollment mode of the course where banner appears
     * @param screenName     The screen name where banner appears
     * @param bannerType     Type of course date banner
     */
    void trackPLSCourseDatesBanner(@NonNull String biValue, @NonNull String courseId, @NonNull String enrollmentMode,
                                   @NonNull String screenName, @NonNull String bannerType);

    /**
     * Track the Shift Dates button tapped on PLS Course Dates Banner
     *
     * @param courseId       course id of the course where banner appears
     * @param enrollmentMode enrollment mode of the course where button will be tapped
     * @param screenName     The screen name on which button will be tapped
     */
    void trackPLSShiftButtonTapped(@NonNull String courseId, @NonNull String enrollmentMode,
                                   @NonNull String screenName);

    /**
     * Track that PLS Course Dates Shifted successfully or not
     *
     * @param courseId       course id of the course where banner appears
     * @param enrollmentMode enrollment mode of the course on which dates will be shifted
     * @param screenName     The screen name on which dates will be shifted
     * @param isSuccess      Does the shifted successful or not
     */
    void trackPLSCourseDatesShift(@NonNull String courseId, @NonNull String enrollmentMode,
                                  @NonNull String screenName, boolean isSuccess);

    /**
     * Track the Value Prop Modal appearance
     *
     * @param courseId     course id of the course through which the modal is appeared
     * @param assignmentId Assignment id of course unit
     * @param screenName   The screen name through which Modal will appear
     */
    void trackValuePropModalView(@NonNull String courseId, @Nullable String assignmentId,
                                 @NonNull String screenName);

    /**
     * Track the Value Prop Learn more button tapped
     *
     * @param courseId     course id of the course on which button is tapped
     * @param assignmentId Assignment id of course unit
     * @param screenName   The screen name on which button will be tapped
     */
    void trackValuePropLearnMoreTapped(@NonNull String courseId, @Nullable String assignmentId,
                                       @NonNull String screenName);


    interface Keys {
        String NAME = "name";
        String USER_ID = "user_id";
        String OLD_TIME = "old_time";
        String NEW_TIME = "new_time";
        String NEW_SPEED = "new_speed";
        String OLD_SPEED = "old_speed";
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
        String ASSIGNMENT_ID = "assignment_id";
        String SUPPORTED = "supported";
        String DEVICE_ORIENTATION = "device-orientation";
        String MODE = "mode";
        String SCREEN_NAME = "screen_name";
        String BANNER_TYPE = "banner_type";
        String SUCCESS = "success";

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
        // Bulk download feature keys
        String TOTAL_DOWNLOADABLE_VIDEOS = "total_downloadable_videos";
        String REMAINING_DOWNLOADABLE_VIDEOS = "remaining_downloadable_videos";
        // Subjects
        String SUBJECT_ID = "subject_id";
        // Firebase Remote Configs keys for A/A test
        // Ref: https://openedx.atlassian.net/browse/LEARNER-7394
        String EXPERIMENT = "experiment";
        String GROUP = "group";
        String AA_EXPERIMENT = "aa_experiment";
        // Video Play Medium
        String PLAY_MEDIUM = "play_medium";
        // Used to access the analytics data in middle ware
        String EVENT = "event";
        String PROPERTIES = "properties";
    }

    interface Values {
        String SCREEN = "screen";
        String SKIP = "skip";
        String SLIDE = "slide";
        String MOBILE = "mobile";
        String VIDEOPLAYER = "videoplayer";
        String PASSWORD = "Password";
        String FACEBOOK = "Facebook";
        String GOOGLE = "Google";
        String MICROSOFT = "Microsoft";
        String DOWNLOAD_MODULE = "downloadmodule";
        String VIDEO_LOADED = "edx.video.loaded";
        String VIDEO_PLAYED = "edx.video.played";
        String VIDEO_PAUSED = "edx.video.paused";
        String VIDEO_STOPPED = "edx.video.stopped";
        //The seek event name has been changed as per MOB-1273
        String VIDEO_SEEKED = "edx.video.position.changed";
        String TRANSCRIPT_SHOWN = "edx.video.transcript.shown";
        String VIDEO_PLAYBACK_SPEED_CHANGED = "edx.bi.video.speed.changed";
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
        String DISCOVERY_COURSES_SEARCH = "edx.bi.app.discovery.courses_search";
        String USER_FIND_COURSES = "edx.bi.app.search.find_courses.clicked";
        String CREATE_ACCOUNT_CLICKED = "edx.bi.app.user.register.clicked";
        String USER_REGISTRATION_SUCCESS = "edx.bi.app.user.register.success";
        String USER_COURSE_ENROLL_CLICKED = "edx.bi.app.course.enroll.clicked";
        String USER_COURSE_ENROLL_SUCCESS = "edx.bi.app.course.enroll.success";
        String USER_COURSE_UPGRADE_SUCCESS = "edx.bi.app.course.upgrade.success";
        String VALUE_PROP_LEARN_MORE_CLICKED = "edx.bi.app.value.prop.learn.more.clicked";
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
        // Bulk download feature event values
        String BULK_DOWNLOAD_SWITCH_ON = "edx.bi.app.videos.download.toggle.on";
        String BULK_DOWNLOAD_SWITCH_OFF = "edx.bi.app.videos.download.toggle.off";
        // Discovery Courses Search
        String DISCOVERY_COURSES_SEARCH_LANDING = "landing_screen";
        String DISCOVERY_COURSES_SEARCH_TAB = "discovery_tab";
        // Subjects
        String SUBJECT_CLICKED = "edx.bi.app.discover.subject.clicked";
        String DISCOVERY = "discovery";
        String VIEW_ALL_SUBJECTS = "View All Subjects";
        // Settings event values
        String DOWNLOAD_TO_SD_CARD_SWITCH_ON = "edx.bi.app.settings.sdcard.toggle.on";
        String DOWNLOAD_TO_SD_CARD_SWITCH_OFF = "edx.bi.app.settings.sdcard.toggle.off";
        // Cast device connection state
        String CAST_CONNECTED = "edx.bi.app.cast.connected";
        String CAST_DISCONNECTED = "edx.bi.app.cast.disconnected";
        String VIDEO_CASTED = "edx.bi.app.cast.video_casted";
        // -- Play mediums --
        // Casting Device Types
        String GOOGLE_CAST = "google_cast";
        // YouTube Player Type
        String YOUTUBE = "youtube";
        // PLS Course Dates Banner
        String COURSE_DATES = "course_dates";
        String COURSE_DATES_BANNER_INFO = "edx.bi.app.coursedates.info";
        String COURSE_DATES_BANNER_UPGRADE_TO_PARTICIPATE = "edx.bi.app.coursedates.upgrade.participate";
        String COURSE_DATES_BANNER_UPGRADE_TO_SHIFT = "edx.bi.app.coursedates.shift";
        String COURSE_DATES_BANNER_SHIFT_DATES = "edx.bi.app.coursedates.upgrade.shift";
        String PLS_BANNER_TYPE_INFO = "info";
        String PLS_BANNER_TYPE_UPGRADE_TO_PARTICIPATE = "upgrade_to_participate";
        String PLS_BANNER_TYPE_UPGRADE_TO_SHIFT = "upgrade_to_shift";
        String PLS_BANNER_TYPE_SHIFT_DATES = "shift_dates";

    }

    interface Screens {
        String COURSE_INFO_SCREEN = "Course Info";
        String PROGRAM_INFO_SCREEN = "Program Info";
        String LAUNCH_ACTIVITY = "Launch";
        String REGISTER = "Register";
        String LOGIN = "Login";
        String COURSE_ENROLLMENT = "course_enrollment";
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
        String FIND_PROGRAMS = "Find Programs";
        String FIND_DEGREES = "Find Degrees";
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
        String ALL_SUBJECTS = "Discover: All Subjects";
        String PAYMENTS_INFO_SCREEN = "Payments info";
        String COURSE_UNIT_LOCKED = "Course unit locked";
        String PLACE_ORDER_COURSE_UPGRADE = "Place order: course upgrade";
        String PLS_COURSE_DASHBOARD = "course_dashboard";
        String PLS_COURSE_DATES = "dates_screen";
        String PLS_COURSE_UNIT_ASSIGNMENT = "assignments_screen";
    }

    interface Events {
        String LOADED_VIDEO = "Loaded Video";
        String PLAYED_VIDEO = "Played Video";
        String PAUSED_VIDEO = "Paused Video";
        String STOPPED_VIDEO = "Stopped Video";
        String SEEK_VIDEO = "Seeked Video";
        String SHOW_TRANSCRIPT = "Show Transcript";
        String SPEED_CHANGE_VIDEO = "Speed Change Video";
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
        String REGISTRATION_SUCCESS = "Registration Success";
        String COURSE_ENROLL_CLICKED = "Course Enroll Clicked";
        String COURSE_ENROLL_SUCCESS = "Course Enroll Success";
        String COURSE_UPGRADE_SUCCESS = "Course Upgrade Success";
        String DISCOVERY_COURSES_SEARCH = "Discovery: Courses Search";
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
        // Bulk download events
        String BULK_DOWNLOAD_TOGGLE_ON = "Bulk Download Toggle On";
        String BULK_DOWNLOAD_TOGGLE_OFF = "Bulk Download Toggle Off";
        String SUBJECT_DISCOVERY = "Subject Discovery";
        // Settings events
        String DOWNLOAD_TO_SD_CARD_ON = "Download to sd-card On";
        String DOWNLOAD_TO_SD_CARD_OFF = "Download to sd-card Off";
        // Firebase Remote Configs Event name for A/A test
        // Ref: https://openedx.atlassian.net/browse/LEARNER-7394
        String MOBILE_EXPERIMENT_EVALUATED = "Mobile Experiment Evaluated";
        // Casting Devices Event
        String CAST_CONNECTED = "Cast: Connected";
        String CAST_DISCONNECTED = "Cast: Disconnected";
        String VIDEO_CASTED = "Cast: Video Casted";
        // PLS Course Dates Banner
        String PLS_BANNER_VIEWED = "PLS Banner Viewed";
        String PLS_SHIFT_DATES_BUTTON_TAPPED = "PLS Shift Button Tapped";
        String PLS_SHIFT_DATES = "PLS Shift Dates";
        // Value Prop
        String VALUE_PROP_LEARN_MORE_CLICKED = "Value Prop Learn More Clicked";
        String VALUE_PROP_MODAL_VIEW = "Value Prop Modal View";
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

        /**
         * Method to remove the un-supported characters by the Firebase Analytics from the
         * given string.
         */
        public static String removeUnSupportedCharacters(String value) {
            return value.replaceAll(":", "_")
                    .replaceAll("-", "_")
                    .replaceAll("__", "_");
        }

        /**
         * Method used to format the Analytics data as per firebase recommendations
         * Ref: https://stackoverflow.com/questions/44421234/firebase-analytics-custom-list-of-values
         */
        public static Properties formatFirebaseAnalyticsData(Object object) {
            Properties properties = (Properties) object;
            Properties newProperties = new Properties();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                String entryValueString = String.valueOf(entry.getValue());
                if (entryValueString.length() > 100) {
                    // Truncate to first 100 characters
                    entryValueString = entryValueString.trim().substring(0, 100);
                }
                newProperties.put(Analytics.Util.removeUnSupportedCharacters(key), entryValueString);
            }
            return newProperties;
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
