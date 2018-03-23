package org.edx.mobile.module.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.images.ShareUtils;

import java.util.Map;

/**
 * A concrete implementation of {@link Analytics} to report all the screens and events to Answers.
 */
@Singleton
public class AnswersAnalytics implements Analytics {
    private static final Logger logger = new Logger(AnswersAnalytics.class.getName());

    @NonNull
    private final Answers tracker;

    public AnswersAnalytics() {
        this.tracker = Answers.getInstance();
    }

    private void trackAnswersEvent(@NonNull AnswersEvent event) {
        logger.debug(event.toString());
        tracker.logCustom(event);
    }

    @Override
    public void trackScreenView(@NonNull String screenName, @Nullable String courseId, @Nullable String action, @Nullable Map<String, String> values) {

    }

    @Override
    public void trackVideoPlaying(String videoId, Double currentTime, String courseId, String unitUrl) {

    }

    @Override
    public void trackVideoPause(String videoId, Double currentTime, String courseId, String unitUrl) {

    }

    @Override
    public void trackVideoStop(String videoId, Double currentTime, String courseId, String unitUrl) {

    }

    @Override
    public void trackShowTranscript(String videoId, Double currentTime, String courseId, String unitUrl) {

    }

    @Override
    public void trackHideTranscript(String videoId, Double currentTime, String courseId, String unitUrl) {

    }

    @Override
    public void trackVideoLoading(String videoId, String courseId, String unitUrl) {

    }

    @Override
    public void trackVideoSeek(String videoId, Double oldTime, Double newTime, String courseId, String unitUrl, Boolean skipSeek) {

    }

    @Override
    public void trackDownloadComplete(String videoId, String courseId, String unitUrl) {

    }

    @Override
    public void trackBrowserLaunched(String url) {

    }

    @Override
    public void trackSubSectionBulkVideoDownload(String section, String subSection, String enrollmentId, long videoCount) {

    }

    @Override
    public void trackUserLogin(String method) {

    }

    @Override
    public void trackUserLogout() {

    }

    @Override
    public void trackTranscriptLanguage(String videoId, Double currentTime, String lang, String courseId, String unitUrl) {

    }

    @Override
    public void trackSingleVideoDownload(String videoId, String courseId, String unitUrl) {

    }

    @Override
    public void trackVideoOrientation(String videoId, Double currentTime, boolean isLandscape, String courseId, String unitUrl) {

    }

    @Override
    public void trackDiscoverCoursesClicked() {

    }

    @Override
    public void trackExploreSubjectsClicked() {

    }

    @Override
    public void trackUserSignUpForAccount() {

    }

    @Override
    public void trackUserFindsCourses() {

    }

    @Override
    public void trackCreateAccountClicked(@NonNull String appVersion, @Nullable String source) {

    }

    @Override
    public void trackRegistrationSuccess(@NonNull String appVersion, @Nullable String source) {
        final AnswersEvent event = new AnswersEvent(Events.REGISTRATION_SUCCESS);
        event.putCustomAttribute(Keys.NAME, Values.USER_REGISTRATION_SUCCESS);
        if (!TextUtils.isEmpty(source)) {
            event.putCustomAttribute(Keys.PROVIDER, source);
        }

        event.addCategoryToBiEvents(Values.CONVERSION, appVersion);
        trackAnswersEvent(event);
    }

    @Override
    public void trackEnrollClicked(@NonNull String courseId, boolean emailOptIn) {

    }

    @Override
    public void trackEnrolmentSuccess(@NonNull String courseId, boolean emailOptIn) {
        final AnswersEvent event = new AnswersEvent(Events.COURSE_ENROLL_SUCCESS);
        event.putCustomAttribute(Keys.NAME, Values.USER_COURSE_ENROLL_SUCCESS)
                .putCustomAttribute(Analytics.Keys.COURSE_ID, courseId)
                .putCustomAttribute(Keys.EMAIL_OPT_IN, emailOptIn ? 1 : 0)
        ;

        event.addCategoryToBiEvents(Values.CONVERSION, courseId);
        trackAnswersEvent(event);
    }

    @Override
    public void trackNotificationReceived(@Nullable String courseId) {

    }

    @Override
    public void trackNotificationTapped(@Nullable String courseId) {

    }

    @Override
    public void trackUserConnectionSpeed(String connectionType, float connectionSpeed) {

    }

    @Override
    public void certificateShared(String courseId, String certificateUrl, ShareUtils.ShareType method) {

    }

    @Override
    public void courseDetailShared(String courseId, String aboutUrl, ShareUtils.ShareType method) {

    }

    @Override
    public void trackCourseComponentViewed(String blockId, String courseId, String minifiedBlockId) {

    }

    @Override
    public void trackOpenInBrowser(String blockId, String courseId, boolean isSupported, String minifiedBlockId) {

    }

    @Override
    public void trackProfileViewed(@NonNull String username) {

    }

    @Override
    public void trackProfilePhotoSet(boolean fromCamera) {

    }

    @Override
    public void identifyUser(String userID, String email, String username) {

    }

    @Override
    public void resetIdentifyUser() {

    }

    @Override
    public void trackAppRatingDialogViewed(String versionName) {

    }

    @Override
    public void trackAppRatingDialogCancelled(String versionName) {

    }

    @Override
    public void trackUserSubmitRating(String versionName, int rating) {

    }

    @Override
    public void trackUserSendFeedback(String versionName, int rating) {

    }

    @Override
    public void trackUserMayReviewLater(String versionName, int rating) {

    }

    @Override
    public void trackRateTheAppClicked(String versionName, int rating) {

    }

    @Override
    public void trackWhatsNewClosed(@NonNull String versionName, int totalViewed, int currentlyViewed, int totalScreens) {

    }

    @Override
    public void trackWhatsNewSeen(@NonNull String versionName, int totalScreens) {

    }

    @Override
    public void trackSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId) {

    }

    @Override
    public void trackUndoingSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId) {

    }

    @Override
    public void trackUnitVideoDelete(@NonNull String courseId, @NonNull String unitId) {

    }

    @Override
    public void trackUndoingUnitVideoDelete(@NonNull String courseId, @NonNull String unitId) {

    }

    @Override
    public void trackBulkDownloadSwitchOn(@NonNull String courseId, int totalDownloadableVideos, int remainingDownloadableVideos) {

    }

    @Override
    public void trackBulkDownloadSwitchOff(@NonNull String courseId, int totalDownloadableVideos) {

    }
}
