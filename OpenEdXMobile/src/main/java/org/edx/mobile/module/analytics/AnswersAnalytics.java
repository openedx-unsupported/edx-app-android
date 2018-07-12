package org.edx.mobile.module.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SearchEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.google.inject.Singleton;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.images.ShareUtils;

import java.util.Map;

import static org.edx.mobile.module.analytics.Analytics.Util.getShareTypeValue;

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
        final LoginEvent event = new LoginEvent();
        AnswersEventUtil.setCustomProperties(event);
        if (method != null) {
            event.putMethod(method);
        }
        event.putSuccess(true);
        event.putCustomAttribute(Keys.NAME, Values.USERLOGIN);
        tracker.logLogin(event);
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
    public void trackCoursesSearch(String searchQuery, boolean isLoggedIn, String versionName) {
        final SearchEvent event = new SearchEvent();
        AnswersEventUtil.setCustomProperties(event);

        event.putQuery(searchQuery);
        event.putCustomAttribute(Keys.NAME, Values.DISCOVERY_COURSES_SEARCH)
                .putCustomAttribute(Keys.APP_VERSION, versionName)
                .putCustomAttribute(Keys.ACTION, isLoggedIn ? Values.DISCOVERY_COURSES_SEARCH_TAB : Values.DISCOVERY_COURSES_SEARCH_LANDING);
        tracker.logSearch(event);
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
        final CustomEvent event = new CustomEvent(Events.REGISTRATION_SUCCESS);
        AnswersEventUtil.setCustomProperties(event);
        AnswersEventUtil.addCategoryToBiEvents(event, Values.CONVERSION, appVersion);

        event.putCustomAttribute(Keys.NAME, Values.USER_REGISTRATION_SUCCESS);
        if (!TextUtils.isEmpty(source)) {
            event.putCustomAttribute(Keys.PROVIDER, source);
        }
        tracker.logCustom(event);
    }

    @Override
    public void trackEnrollClicked(@NonNull String courseId, boolean emailOptIn) {

    }

    @Override
    public void trackEnrolmentSuccess(@NonNull String courseId, boolean emailOptIn) {
        final CustomEvent event = new CustomEvent(Events.COURSE_ENROLL_SUCCESS);
        AnswersEventUtil.setCustomProperties(event);
        AnswersEventUtil.addCategoryToBiEvents(event, Values.CONVERSION, courseId);

        event.putCustomAttribute(Keys.NAME, Values.USER_COURSE_ENROLL_SUCCESS)
                .putCustomAttribute(Analytics.Keys.COURSE_ID, courseId)
                .putCustomAttribute(Keys.EMAIL_OPT_IN, emailOptIn ? 1 : 0);
        tracker.logCustom(event);
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
        final ShareEvent event = new ShareEvent();
        AnswersEventUtil.setCustomProperties(event);
        event.putContentId(courseId);
        event.putContentName(Values.SOCIAL_COURSE_DETAIL_SHARED);
        event.putContentType(Values.SOCIAL_SHARING);
        event.putMethod(getShareTypeValue(method));
        event.putCustomAttribute(Keys.URL, aboutUrl);
        tracker.logShare(event);
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

    @Override
    public void trackSubjectClicked(@NonNull String subjectId) {

    }
}
