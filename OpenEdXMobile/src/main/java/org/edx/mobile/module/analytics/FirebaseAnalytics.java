package org.edx.mobile.module.analytics;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.JavaUtil;
import org.edx.mobile.util.images.ShareUtils;

import java.util.Map;

import static org.edx.mobile.module.analytics.Analytics.Util.getShareTypeValue;

/**
 * A concrete implementation of {@link Analytics} to report all the screens and events to Firebase.
 */
@Singleton
public class FirebaseAnalytics implements Analytics {

    protected final Logger logger = new Logger(getClass().getName());
    @NonNull
    private com.google.firebase.analytics.FirebaseAnalytics tracker;

    @Inject
    public FirebaseAnalytics(@NonNull Context context) {
        tracker = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
    }


    /**
     * This function is used to send the event to Firebase and log the output.
     *
     * @param eventName     The name of the event.
     * @param eventBundle   The Bundle for the event.
     */
    private void logFirebaseEvent(@NonNull String eventName, @NonNull Bundle eventBundle) {
        String csv = eventName;
        for (String parameterName : eventBundle.keySet()) {
            csv += "," + parameterName + "," + eventBundle.get(parameterName);
        }
        logger.debug(csv);
        tracker.logEvent(eventName, eventBundle);
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

        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoLoading(String videoId, String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.LOADED_VIDEO, videoId, Values.VIDEO_LOADED);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoPlaying(String videoId, Double currentTime,
                                  String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.PLAYED_VIDEO, videoId,
                Values.VIDEO_PLAYED, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoPause(String videoId,
                                Double currentTime, String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.PAUSED_VIDEO,
                videoId, Values.VIDEO_PAUSED, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoStop(String videoId, Double currentTime, String courseId,
                               String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.STOPPED_VIDEO,
                videoId, Values.VIDEO_STOPPED, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
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

        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackShowTranscript(String videoId, Double currentTime, String courseId,
                                    String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.SHOW_TRANSCRIPT, videoId,
                Values.TRANSCRIPT_SHOWN, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackHideTranscript(String videoId, Double currentTime, String courseId,
                                    String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.HIDE_TRANSCRIPT,
                videoId, Values.TRANSCRIPT_HIDDEN, currentTime);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackDownloadComplete(String videoId, String courseId,
                                      String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.VIDEO_DOWNLOADED, videoId, Values.VIDEO_DOWNLOADED);
        event.setCourseContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);
        logFirebaseEvent(event.getName(), event.getBundle());
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
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackSingleVideoDownload(String videoId, String courseId,
                                         String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.SINGLE_VIDEO_DOWNLOAD, videoId,
                Values.SINGLE_VIDEO_DOWNLOAD);
        event.setCourseContext(courseId, unitUrl, Values.DOWNLOAD_MODULE);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackVideoOrientation(String videoId, Double currentTime,
                                      boolean isLandscape, String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.SCREEN_TOGGLED, videoId,
                Values.FULLSREEN_TOGGLED, currentTime);
        event.putBoolean(Keys.FULLSCREEN, isLandscape);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackCoursesSearch(String searchQuery, boolean isLoggedIn, String versionName) {
        final FirebaseEvent event = new FirebaseEvent(Events.DISCOVERY_COURSES_SEARCH,
                Values.DISCOVERY_COURSES_SEARCH);
        event.putString(Keys.LABEL, searchQuery);
        event.putString(Keys.APP_VERSION, versionName);
        event.putString(Keys.ACTION, isLoggedIn ? Values.DISCOVERY_COURSES_SEARCH_TAB : Values.DISCOVERY_COURSES_SEARCH_LANDING);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserLogin(String method) {
        final FirebaseEvent event = new FirebaseEvent(Events.USER_LOGIN, Values.USERLOGIN);
        //More information regarding a track event should be under 'data'
        if (method != null) {
            event.putString(Keys.METHOD, method);
        }
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserLogout() {
        final FirebaseEvent event = new FirebaseEvent(Events.USER_LOGOUT, Values.USERLOGOUT);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackBrowserLaunched(String url) {
        final FirebaseEvent event = new FirebaseEvent(Events.BROWSER_LAUNCHED,
                Values.BROWSER_LAUNCHED);
        if (url != null) {
            event.putString(Keys.TARGET_URL, url);
        }
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackTranscriptLanguage(String videoId, Double currentTime, String lang,
                                        String courseId, String unitUrl) {
        final FirebaseEvent event = new FirebaseEvent(Events.LANGUAGE_CLICKED, videoId,
                Values.TRANSCRIPT_LANGUAGE, currentTime);
        event.putString(Keys.LANGUAGE, lang);
        event.setCourseContext(courseId, unitUrl, Values.VIDEOPLAYER);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserSignUpForAccount() {
        final FirebaseEvent event = new FirebaseEvent(Events.SIGN_UP, Values.USER_NO_ACCOUNT);
        logFirebaseEvent(event.getName(), event.getBundle());

    }

    @Override
    public void trackUserFindsCourses() {
        final FirebaseEvent event = new FirebaseEvent(Events.FIND_COURSES,
                Values.USER_FIND_COURSES);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.USER_ENGAGEMENT, Values.COURSE_DISCOVERY);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackCreateAccountClicked(@NonNull String appVersion, @Nullable String source) {
        final FirebaseEvent event = new FirebaseEvent(Events.CREATE_ACCOUNT_CLICKED,
                Values.CREATE_ACCOUNT_CLICKED);
        if (!TextUtils.isEmpty(source))
            event.putString(Keys.PROVIDER, source);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.CONVERSION, appVersion);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackRegistrationSuccess(@NonNull String appVersion, @Nullable String source) {
        final FirebaseEvent event = new FirebaseEvent(Events.REGISTRATION_SUCCESS,
                Values.USER_REGISTRATION_SUCCESS);
        if (!TextUtils.isEmpty(source)) {
            event.putString(Keys.PROVIDER, source);
        }

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.CONVERSION, appVersion);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackEnrollClicked(@NonNull String courseId, boolean email_opt_in) {
        final FirebaseEvent event = new FirebaseEvent(Events.COURSE_ENROLL_CLICKED,
                Values.USER_COURSE_ENROLL_CLICKED);
        event.putCourseId(courseId);
        event.putBoolean(Keys.EMAIL_OPT_IN, email_opt_in);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.CONVERSION, courseId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackEnrolmentSuccess(@NonNull String courseId, boolean emailOptIn) {
        final FirebaseEvent event = new FirebaseEvent(Events.COURSE_ENROLL_SUCCESS,
                Values.USER_COURSE_ENROLL_SUCCESS);
        event.putCourseId(courseId);
        event.putBoolean(Keys.EMAIL_OPT_IN, emailOptIn);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.CONVERSION, courseId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserConnectionSpeed(String connectionType, float connectionSpeed) {
        final FirebaseEvent event = new FirebaseEvent(Events.SPEED, Values.CONNECTION_SPEED);
        event.putString(Keys.CONNECTION_TYPE, connectionType);
        event.putFloat(Keys.CONNECTION_SPEED, connectionSpeed);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackNotificationReceived(@Nullable String courseId) {
        final FirebaseEvent event = new FirebaseEvent(Events.PUSH_NOTIFICATION_RECEIVED,
                Values.NOTIFICATION_RECEIVED);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.PUSH_NOTIFICATION, courseId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackNotificationTapped(@Nullable String courseId) {
        final FirebaseEvent event = new FirebaseEvent(Events.PUSH_NOTIFICATION_TAPPED,
                Values.NOTIFICATION_TAPPED);

        //Add category for Google Analytics
        event.addCategoryToBiEvents(Values.PUSH_NOTIFICATION, courseId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void courseDetailShared(String courseId, String aboutUrl, ShareUtils.ShareType method) {
        final FirebaseEvent event = new FirebaseEvent(Events.SOCIAL_COURSE_DETAIL_SHARED,
                Values.SOCIAL_COURSE_DETAIL_SHARED);
        event.putCourseId(courseId);
        event.putString(Keys.CATEGORY, Values.SOCIAL_SHARING);
        event.putString(Keys.URL, aboutUrl);
        event.putString(Keys.TYPE, getShareTypeValue(method));
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void certificateShared(String courseId, String certificateUrl, ShareUtils.ShareType method) {
        final FirebaseEvent event = new FirebaseEvent(Events.SOCIAL_CERTIFICATE_SHARED,
                Values.SOCIAL_CERTIFICATE_SHARED);
        event.putCourseId(courseId);
        event.putString(Keys.CATEGORY, Values.SOCIAL_SHARING);
        event.putString(Keys.URL, certificateUrl);
        event.putString(Keys.TYPE, getShareTypeValue(method));
        logFirebaseEvent(event.getName(), event.getBundle());
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
        logFirebaseEvent(event.getName(), event.getBundle());
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
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackProfileViewed(@NonNull String username) {
        final FirebaseEvent event = new FirebaseEvent(Events.PROFILE_VIEWED, Values.PROFILE_VIEWED);
        event.addCategoryToBiEvents(Values.PROFILE, username);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackProfilePhotoSet(boolean fromCamera) {
        final FirebaseEvent event = new FirebaseEvent(Events.PROFILE_PHOTO_SET,
                Values.PROFILE_PHOTO_SET);
        event.addCategoryToBiEvents(Values.PROFILE, fromCamera ? Values.CAMERA : Values.LIBRARY);
        logFirebaseEvent(event.getName(), event.getBundle());
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

    @Override
    public void trackAppRatingDialogViewed(String versionName) {
        final FirebaseEvent event = new FirebaseEvent(Events.APP_REVIEWS_VIEW_RATING,
                Values.APP_REVIEWS_VIEW_RATING);
        event.putString(Keys.CATEGORY, Values.APP_REVIEWS_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackAppRatingDialogCancelled(String versionName) {
        final FirebaseEvent event = new FirebaseEvent(Events.APP_REVIEWS_DISMISS_RATING,
                Values.APP_REVIEWS_DISMISS_RATING);
        event.putString(Keys.CATEGORY, Values.APP_REVIEWS_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserSubmitRating(String versionName, int rating) {
        final FirebaseEvent event = new FirebaseEvent(Events.APP_REVIEWS_SUBMIT_RATING,
                Values.APP_REVIEWS_SUBMIT_RATING);
        event.putString(Keys.CATEGORY, Values.APP_REVIEWS_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        event.putInt(Keys.RATING, rating);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserSendFeedback(String versionName, int rating) {
        final FirebaseEvent event = new FirebaseEvent(Events.APP_REVIEWS_SEND_FEEDBACK,
                Values.APP_REVIEWS_SEND_FEEDBACK);
        event.putString(Keys.CATEGORY, Values.APP_REVIEWS_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        event.putInt(Keys.RATING, rating);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUserMayReviewLater(String versionName, int rating) {
        final FirebaseEvent event = new FirebaseEvent(Events.APP_REVIEWS_MAYBE_LATER,
                Values.APP_REVIEWS_MAYBE_LATER);
        event.putString(Keys.CATEGORY, Values.APP_REVIEWS_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        event.putInt(Keys.RATING, rating);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackRateTheAppClicked(String versionName, int rating) {
        final FirebaseEvent event = new FirebaseEvent(Events.APP_REVIEWS_RATE_THE_APP,
                Values.APP_REVIEWS_RATE_THE_APP);
        event.putString(Keys.CATEGORY, Values.APP_REVIEWS_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        event.putInt(Keys.RATING, rating);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackWhatsNewClosed(@NonNull String versionName, int totalViewed, int currentlyViewed, int totalScreens) {
        final FirebaseEvent event = new FirebaseEvent(Events.WHATS_NEW_CLOSE, Values.WHATS_NEW_CLOSE);
        event.putString(Keys.CATEGORY, Values.WHATS_NEW_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        event.putInt(Keys.TOTAL_VIEWED, totalViewed);
        event.putInt(Keys.CURRENTLY_VIEWED, currentlyViewed);
        event.putInt(Keys.TOTAL_SCREENS, totalScreens);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackWhatsNewSeen(@NonNull String versionName, int totalScreens) {
        final FirebaseEvent event = new FirebaseEvent(Events.WHATS_NEW_DONE, Values.WHATS_NEW_DONE);
        event.putString(Keys.CATEGORY, Values.WHATS_NEW_CATEGORY);
        event.putString(Keys.APP_VERSION, versionName);
        event.putInt(Keys.TOTAL_SCREENS, totalScreens);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId) {
        final FirebaseEvent event = new FirebaseEvent(Events.VIDEOS_SUBSECTION_DELETE, Values.VIDEOS_SUBSECTION_DELETE);
        event.putString(Keys.COURSE_ID, courseId);
        event.putString(Keys.SUBSECTION_ID, subsectionId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUndoingSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId) {
        final FirebaseEvent event = new FirebaseEvent(Events.VIDEOS_UNDO_SUBSECTION_DELETE, Values.VIDEOS_UNDO_SUBSECTION_DELETE);
        event.putString(Keys.COURSE_ID, courseId);
        event.putString(Keys.SUBSECTION_ID, subsectionId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUnitVideoDelete(@NonNull String courseId, @NonNull String unitId) {
        final FirebaseEvent event = new FirebaseEvent(Events.VIDEOS_UNIT_DELETE, Values.VIDEOS_UNIT_DELETE);
        event.putString(Keys.COURSE_ID, courseId);
        event.putString(Keys.UNIT_ID, unitId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackUndoingUnitVideoDelete(@NonNull String courseId, @NonNull String unitId) {
        final FirebaseEvent event = new FirebaseEvent(Events.VIDEOS_UNDO_UNIT_DELETE, Values.VIDEOS_UNDO_UNIT_DELETE);
        event.putString(Keys.COURSE_ID, courseId);
        event.putString(Keys.UNIT_ID, unitId);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackBulkDownloadSwitchOn(@NonNull String courseId, int totalDownloadableVideos, int remainingDownloadableVideos) {
        final FirebaseEvent event = new FirebaseEvent(Events.BULK_DOWNLOAD_TOGGLE_ON, Values.BULK_DOWNLOAD_SWITCH_ON);
        event.putString(Keys.COURSE_ID, courseId);
        event.putString(Keys.COMPONENT, Values.DOWNLOAD_MODULE);
        event.putInt(Keys.TOTAL_DOWNLOADABLE_VIDEOS, totalDownloadableVideos);
        event.putInt(Keys.REMAINING_DOWNLOADABLE_VIDEOS, remainingDownloadableVideos);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackBulkDownloadSwitchOff(@NonNull String courseId, int totalDownloadableVideos) {
        final FirebaseEvent event = new FirebaseEvent(Events.BULK_DOWNLOAD_TOGGLE_OFF, Values.BULK_DOWNLOAD_SWITCH_OFF);
        event.putString(Keys.COURSE_ID, courseId);
        event.putString(Keys.COMPONENT, Values.DOWNLOAD_MODULE);
        event.putInt(Keys.TOTAL_DOWNLOADABLE_VIDEOS, totalDownloadableVideos);
        logFirebaseEvent(event.getName(), event.getBundle());
    }

    @Override
    public void trackSubjectClicked(@NonNull String subjectId) {
        final FirebaseEvent event = new FirebaseEvent(Events.SUBJECT_DISCOVERY, Values.SUBJECT_CLICKED);
        event.putString(Keys.SUBJECT_ID, subjectId);
        event.putString(Keys.CATEGORY, Values.DISCOVERY);
        logFirebaseEvent(event.getName(), event.getBundle());
    }
}
