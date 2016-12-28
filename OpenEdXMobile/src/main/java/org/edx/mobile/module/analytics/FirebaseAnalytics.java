package org.edx.mobile.module.analytics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.util.JavaUtil;
import org.edx.mobile.util.images.ShareUtils;

import java.util.Map;

import static org.edx.mobile.module.analytics.Analytics.Util.getShareTypeValue;

/**
 * A concrete implementation of {@link Analytics} to report all the screens and events to Firebase.
 */
@Singleton
public class FirebaseAnalytics implements Analytics {
    @NonNull
    private com.google.firebase.analytics.FirebaseAnalytics tracker;

    @Inject
    public FirebaseAnalytics(@NonNull Context context) {
        tracker = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
    }

    /**
     * This function is used to send the screen tracking event.
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
        final FirebaseEvent event = new FirebaseEvent(screenName);
        if (!TextUtils.isEmpty(action)) {
            event.putString(Keys.ACTION, action);
        }
        if (!TextUtils.isEmpty(courseId)) {
            event.putCourseId(courseId);
        }
        if (values != null) {
            event.putMap(values);
        }

        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoLoading(String videoId, String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.LOADED_VIDEO, videoId, Values.VIDEO_LOADED);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoPlaying(String videoId, Double currentTime,
                                  String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.PLAYED_VIDEO, videoId,
                Values.VIDEO_PLAYED, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoPause(String videoId,
                                Double currentTime, String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.PAUSED_VIDEO,
                videoId, Values.VIDEO_PAUSED, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoStop(String videoId, Double currentTime, String courseId,
                               String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.STOPPED_VIDEO,
                videoId, Values.VIDEO_STOPPED, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoSeek(String videoId, Double oldTime, Double newTime, String courseId,
                               String unitUrl, Boolean skipSeek) {
        final FirebaseEvent event = new FirebaseEvent(Events.SEEK_VIDEO, videoId, Values.VIDEO_SEEKED);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        //Call the format Double value so that we can have upto 3 decimal places after
        oldTime = JavaUtil.formatDoubleValue(oldTime, 3);
        newTime = JavaUtil.formatDoubleValue(newTime, 3);
        Double skipInterval = newTime - oldTime;
        skipInterval = JavaUtil.formatDoubleValue(skipInterval, 3);
        event.putDouble(Keys.OLD_TIME, oldTime);
        event.putDouble(Keys.NEW_TIME, newTime);
        if (skipSeek) {
            event.putString(Keys.SEEK_TYPE, Values.SKIP);
        } else {
            event.putString(Keys.SEEK_TYPE, Values.SLIDE);
        }
        event.putDouble(Keys.REQUESTED_SKIP_INTERVAL, skipInterval);

        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackShowTranscript(String videoId, Double currentTime, String courseId,
                                    String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.SHOW_TRANSCRIPT, videoId,
                Values.TRANSCRIPT_SHOWN, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackHideTranscript(String videoId, Double currentTime, String courseId,
                                    String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.HIDE_TRANSCRIPT,
                videoId, Values.TRANSCRIPT_HIDDEN, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackDownloadComplete(String videoId, String courseId,
                                      String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.VIDEO_DOWNLOADED, videoId, Values.VIDEO_DOWNLOADED);
        event.setCourseContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackSubSectionBulkVideoDownload(String section, String subSection,
                                                 String enrollmentId, long videoCount) {
        final FirebaseEvent event = new FirebaseEvent(Events.BULK_DOWNLOAD_SUBSECTION,
                Values.BULK_DOWNLOAD_SUBSECTION);
        if (section != null && subSection != null) {
            event.putString(Keys.COURSE_SECTION, section);
            event.putString(Keys.COURSE_SUBSECTION, subSection);
        }
        event.putLong(Keys.NO_OF_VIDEOS, videoCount);
        event.setCourseContext(enrollmentId, null, Values.DOWNLOAD_MODULE);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackSingleVideoDownload(String videoId, String courseId,
                                         String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.SINGLE_VIDEO_DOWNLOAD, videoId,
                Values.SINGLE_VIDEO_DOWNLOAD);
        event.setCourseContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoOrientation(String videoId, Double currentTime,
                                      boolean isLandscape, String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.SCREEN_TOGGLED, videoId,
                Values.FULLSREEN_TOGGLED, currentTime);
        event.putBoolean(Keys.FULLSCREEN, isLandscape);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackDiscoverCoursesClicked() {
        final FirebaseEvent event = new FirebaseEvent(Events.DISCOVER_COURSES,
                Values.DISCOVER_COURSES_CLICK);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackExploreSubjectsClicked() {
        final FirebaseEvent event = new FirebaseEvent(Events.EXPLORE_SUBJECTS,
                Values.EXPLORE_SUBJECTS_CLICK);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserLogin(String method) {
        final FirebaseEvent event = new FirebaseEvent(Events.USER_LOGIN, Values.USERLOGIN);
        //More information regarding a track event should be under 'data'
        if (method != null) {
            event.putString(Keys.METHOD, method);
        }
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserLogout() {
        final FirebaseEvent event = new FirebaseEvent(Events.USER_LOGOUT, Values.USERLOGOUT);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackBrowserLaunched(String url) {
        final FirebaseEvent event = new FirebaseEvent(Events.BROWSER_LAUNCHED,
                Values.BROWSER_LAUNCHED);
        if (url != null) {
            event.putString(Keys.TARGET_URL, url);
        }
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackTranscriptLanguage(String videoId, Double currentTime, String lang,
                                        String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.LANGUAGE_CLICKED, videoId,
                Values.TRANSCRIPT_LANGUAGE, currentTime);
        event.putString(Keys.LANGUAGE, lang);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserSignUpForAccount() {
        final FirebaseEvent event = new FirebaseEvent(Events.SIGN_UP, Values.USER_NO_ACCOUNT);
        tracker.logEvent(event.getName(), event.getBundle());

    }

    @Override
    public void trackUserFindsCourses() {
        final FirebaseEvent event = new FirebaseEvent(Events.FIND_COURSES,
                Values.USER_FIND_COURSES);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.USER_ENGAGEMENT, Values.COURSE_DISCOVERY);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackCreateAccountClicked(String appVersion, String source) {
        final FirebaseEvent event = new FirebaseEvent(Events.CREATE_ACCOUNT_CLICKED,
                Values.CREATE_ACCOUNT_CLICK);
        if (!TextUtils.isEmpty(source))
            event.putString(Keys.PROVIDER, source);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.CONVERSION, appVersion);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackEnrollClicked(String courseId, boolean email_opt_in) {
        final FirebaseEvent event = new FirebaseEvent(Events.ENROLL_COURSES,
                Values.USER_COURSE_ENROLL);
        event.putCourseId(courseId);
        event.putBoolean(Keys.EMAIL_OPT_IN, email_opt_in);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.CONVERSION, courseId);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserConnectionSpeed(String connectionType, float connectionSpeed) {
        final FirebaseEvent event = new FirebaseEvent(Events.SPEED, Values.CONNECTION_SPEED);
        event.putString(Keys.CONNECTION_TYPE, connectionType);
        event.putFloat(Keys.CONNECTION_SPEED, connectionSpeed);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackNotificationReceived(@Nullable String courseId) {
        final FirebaseEvent event = new FirebaseEvent(Events.PUSH_NOTIFICATION_RECEIVED,
                Values.NOTIFICATION_RECEIVED);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.PUSH_NOTIFICATION, courseId);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackNotificationTapped(@Nullable String courseId) {
        final FirebaseEvent event = new FirebaseEvent(Events.PUSH_NOTIFICATION_TAPPED,
                Values.NOTIFICATION_TAPPED);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.PUSH_NOTIFICATION, courseId);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void courseDetailShared(String courseId, String aboutUrl, ShareUtils.ShareType method) {
        final FirebaseEvent event = new FirebaseEvent(Events.SOCIAL_COURSE_DETAIL_SHARED,
                Values.SOCIAL_COURSE_DETAIL_SHARED);
        event.putCourseId(courseId);
        event.putString(Keys.CATEGORY, Values.SOCIAL_SHARING);
        event.putString(Keys.URL, aboutUrl);
        event.putString(Keys.TYPE, getShareTypeValue(method));
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void certificateShared(String courseId, String certificateUrl, ShareUtils.ShareType method) {
        final FirebaseEvent event = new FirebaseEvent(Events.SOCIAL_CERTIFICATE_SHARED,
                Values.SOCIAL_CERTIFICATE_SHARED);
        event.putCourseId(courseId);
        event.putString(Keys.CATEGORY, Values.SOCIAL_SHARING);
        event.putString(Keys.URL, certificateUrl);
        event.putString(Keys.TYPE, getShareTypeValue(method));
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackCourseComponentViewed(String blockId, String courseId,
                                           String minifiedBlockId) {
        final FirebaseEvent event = new FirebaseEvent(Events.COMPONENT_VIEWED,
                Values.COMPONENT_VIEWED);
        event.putCourseId(courseId);
        event.putString(Keys.BLOCK_ID, minifiedBlockId);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.NAVIGATION, Keys.COMPONENT_VIEWED);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackOpenInBrowser(String blockId, String courseId, boolean isSupported,
                                   String minifiedBlockId) {
        final FirebaseEvent event = new FirebaseEvent(Events.OPEN_IN_BROWSER,
                Values.OPEN_IN_BROWSER);
        event.putCourseId(courseId);
        event.putString(Keys.BLOCK_ID, minifiedBlockId);
        event.putBoolean(Keys.SUPPORTED, isSupported);

        //Add category for Google Analytics
        String label = (isSupported ? Values.OPEN_IN_WEB_SUPPORTED : Values.OPEN_IN_WEB_NOT_SUPPORTED);
        event.addCategoryToBiEvents(Values.NAVIGATION, label);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackProfileViewed(@NonNull String username) {
        final FirebaseEvent event = new FirebaseEvent(Events.PROFILE_VIEWED, Values.PROFILE_VIEWED);
        event.addCategoryToBiEvents(Values.PROFILE, username);
        tracker.logEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackProfilePhotoSet(boolean fromCamera) {
        final FirebaseEvent event = new FirebaseEvent(Events.PROFILE_PHOTO_SET,
                Values.PROFILE_PHOTO_SET);
        event.addCategoryToBiEvents(Values.PROFILE, fromCamera ? Values.CAMERA : Values.LIBRARY);
        tracker.logEvent(event.getName(), event.getBundle());
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
        tracker.setUserId(userID);
    }

    /**
     * This resets the Identify user once the user has logged out
     */
    @Override
    public void resetIdentifyUser() {
        tracker.setUserId(null);
    }
}
