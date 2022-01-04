package org.edx.mobile.module.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Singleton;

import org.edx.mobile.model.video.VideoQuality;
import org.edx.mobile.util.images.ShareUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A registry for enabled Analytics implementations, that delegates all methods to them.
 */
@Singleton
public class AnalyticsRegistry implements Analytics {
    @NonNull
    private List<Analytics> services = new ArrayList<>();

    public void addAnalyticsProvider(@NonNull Analytics provider) {
        services.add(provider);
    }

    public void trackScreenView(String screenName) {
        trackScreenView(screenName, null, null);
    }

    public void trackScreenView(@NonNull String screenName, @Nullable String courseId,
                                @Nullable String action) {
        trackScreenView(screenName, courseId, action, null);
    }

    @Override
    public void trackScreenView(@NonNull String screenName, @Nullable String courseId,
                                @Nullable String action, @Nullable Map<String, String> values) {
        // Remove a key-value pair, if the value for a key is null
        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (entry.getValue() == null) {
                    values.remove(entry.getKey());
                }
            }
        }

        for (Analytics service : services) {
            service.trackScreenView(screenName, courseId, action, values);
        }
    }

    @Override
    public void trackBrowserLaunched(String url) {
        for (Analytics service : services) {
            service.trackBrowserLaunched(url);
        }
    }

    @Override
    public void trackOpenInBrowser(String blockId, String courseId, boolean isSupported,
                                   String minifiedBlockId) {
        for (Analytics service : services) {
            service.trackOpenInBrowser(blockId, courseId, isSupported, minifiedBlockId);
        }
    }

    @Override
    public void trackDownloadComplete(String videoId, String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackDownloadComplete(videoId, courseId, unitUrl);
        }
    }

    @Override
    public void trackCourseUpgradeSuccess(String blockId, String courseId, String minifiedBlockId) {
        for (Analytics service : services) {
            service.trackCourseUpgradeSuccess(blockId, courseId, minifiedBlockId);
        }
    }

    @Override
    public void trackUserFindsCourses() {
        for (Analytics service : services) {
            service.trackUserFindsCourses();
        }
    }

    @Override
    public void trackUserConnectionSpeed(String connectionType, float connectionSpeed) {
        for (Analytics service : services) {
            service.trackUserConnectionSpeed(connectionType, connectionSpeed);
        }
    }

    @Override
    public void certificateShared(String courseId, String certificateURL,
                                  ShareUtils.ShareType method) {
        for (Analytics service : services) {
            service.certificateShared(courseId, certificateURL, method);
        }
    }

    @Override
    public void courseDetailShared(String courseId, String aboutUrl, ShareUtils.ShareType method) {
        for (Analytics service : services) {
            service.courseDetailShared(courseId, aboutUrl, method);
        }
    }

    @Override
    public void trackUserLogin(String method) {
        for (Analytics service : services) {
            service.trackUserLogin(method);
        }
    }

    @Override
    public void trackUserLogout() {
        for (Analytics service : services) {
            service.trackUserLogout();
        }
    }

    @Override
    public void trackEnrollClicked(@NonNull String courseId, boolean email_opt_in) {
        for (Analytics service : services) {
            service.trackEnrollClicked(courseId, email_opt_in);
        }
    }

    @Override
    public void trackEnrolmentSuccess(@NonNull String courseId, boolean email_opt_in) {
        for (Analytics service : services) {
            service.trackEnrolmentSuccess(courseId, email_opt_in);
        }
    }

    @Override
    public void trackNotificationReceived(@Nullable String courseId) {
        for (Analytics service : services) {
            service.trackNotificationReceived(courseId);
        }
    }

    @Override
    public void trackVideoPause(String videoId, Double currentTime,
                                String courseId, String unitUrl, String playMedium) {
        for (Analytics service : services) {
            service.trackVideoPause(videoId, currentTime, courseId, unitUrl, playMedium);
        }
    }

    @Override
    public void trackVideoLoading(String videoId, String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackVideoLoading(videoId, courseId, unitUrl);
        }
    }

    @Override
    public void trackVideoPlaying(String videoId, Double currentTime,
                                  String courseId, String unitUrl, String playMedium) {
        for (Analytics service : services) {
            service.trackVideoPlaying(videoId, currentTime, courseId, unitUrl, playMedium);
        }
    }

    @Override
    public void trackVideoStop(String videoId, Double currentTime, String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackVideoStop(videoId, currentTime, courseId, unitUrl);
        }
    }

    @Override
    public void trackVideoOrientation(String videoId, Double currentTime, boolean isLandscape,
                                      String courseId, String unitUrl, String playMedium) {
        for (Analytics service : services) {
            service.trackVideoOrientation(videoId, currentTime, isLandscape, courseId, unitUrl, playMedium);
        }
    }

    @Override
    public void trackTranscriptLanguage(String videoId, Double currentTime,
                                        String lang, String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackTranscriptLanguage(videoId, currentTime, lang, courseId, unitUrl);
        }
    }

    @Override
    public void trackHideTranscript(String videoId, Double currentTime,
                                    String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackHideTranscript(videoId, currentTime, courseId, unitUrl);
        }
    }

    @Override
    public void trackShowTranscript(String videoId, Double currentTime,
                                    String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackShowTranscript(videoId, currentTime, courseId, unitUrl);
        }
    }

    public void trackVideoSpeed(String videoId, Double currentTime,
                                String courseId, String unitUrl, float oldSpeed, float newSpeed) {
        for (Analytics service : services) {
            service.trackVideoSpeed(videoId, currentTime, courseId, unitUrl, oldSpeed, newSpeed);
        }
    }

    @Override
    public void trackVideoSeek(String videoId, Double oldTime, Double newTime,
                               String courseId, String unitUrl, Boolean skipSeek) {
        for (Analytics service : services) {
            service.trackVideoSeek(videoId, oldTime, newTime, courseId, unitUrl, skipSeek);
        }
    }

    @Override
    public void trackSingleVideoDownload(String videoId, String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackSingleVideoDownload(videoId, courseId, unitUrl);
        }
    }

    @Override
    public void trackUserSignUpForAccount() {
        for (Analytics service : services) {
            service.trackUserSignUpForAccount();
        }
    }

    @Override
    public void trackCourseComponentViewed(String blockId, String courseId, String minifiedBlockId) {
        for (Analytics service : services) {
            service.trackCourseComponentViewed(blockId, courseId, minifiedBlockId);
        }
    }

    @Override
    public void trackCoursesSearch(String searchQuery, boolean isLoggedIn, String versionName) {
        for (Analytics service : services) {
            service.trackCoursesSearch(searchQuery, isLoggedIn, versionName);
        }
    }

    @Override
    public void trackCreateAccountClicked(@NonNull String appVersion, @Nullable String source) {
        for (Analytics service : services) {
            service.trackCreateAccountClicked(appVersion, source);
        }
    }

    @Override
    public void trackRegistrationSuccess(@NonNull String appVersion, @Nullable String source) {
        for (Analytics service : services) {
            service.trackRegistrationSuccess(appVersion, source);
        }
    }

    @Override
    public void trackNotificationTapped(@Nullable String courseId) {
        for (Analytics service : services) {
            service.trackNotificationTapped(courseId);
        }
    }

    @Override
    public void trackProfileViewed(@NonNull String username) {
        for (Analytics service : services) {
            service.trackProfileViewed(username);
        }
    }

    @Override
    public void trackSubSectionBulkVideoDownload(String section, String subSection,
                                                 String enrollmentId, long videoCount) {
        for (Analytics service : services) {
            service.trackSubSectionBulkVideoDownload(section, subSection, enrollmentId, videoCount);
        }
    }

    @Override
    public void trackProfilePhotoSet(boolean fromCamera) {
        for (Analytics service : services) {
            service.trackProfilePhotoSet(fromCamera);
        }
    }

    @Override
    public void identifyUser(String userID, String email, String username) {
        for (Analytics service : services) {
            service.identifyUser(userID, email, username);
        }
    }

    /**
     * This resets the Identify user once the user has logged out
     */
    @Override
    public void resetIdentifyUser() {
        for (Analytics service : services) {
            service.resetIdentifyUser();
        }
    }

    @Override
    public void trackAppRatingDialogViewed(String versionName) {
        for (Analytics service : services) {
            service.trackAppRatingDialogViewed(versionName);
        }
    }

    @Override
    public void trackAppRatingDialogCancelled(String versionName) {
        for (Analytics service : services) {
            service.trackAppRatingDialogCancelled(versionName);
        }
    }

    @Override
    public void trackUserSubmitRating(String versionName, int rating) {
        for (Analytics service : services) {
            service.trackUserSubmitRating(versionName, rating);
        }
    }

    @Override
    public void trackUserSendFeedback(String versionName, int rating) {
        for (Analytics service : services) {
            service.trackUserSendFeedback(versionName, rating);
        }
    }

    @Override
    public void trackUserMayReviewLater(String versionName, int rating) {
        for (Analytics service : services) {
            service.trackUserMayReviewLater(versionName, rating);
        }
    }

    @Override
    public void trackRateTheAppClicked(String versionName, int rating) {
        for (Analytics service : services) {
            service.trackRateTheAppClicked(versionName, rating);
        }
    }

    @Override
    public void trackWhatsNewClosed(@NonNull String versionName, int totalViewed, int currentlyViewed, int totalScreens) {
        for (Analytics service : services) {
            service.trackWhatsNewClosed(versionName, totalViewed, currentlyViewed, totalScreens);
        }
    }

    @Override
    public void trackWhatsNewSeen(@NonNull String versionName, int totalScreens) {
        for (Analytics service : services) {
            service.trackWhatsNewSeen(versionName, totalScreens);
        }
    }

    @Override
    public void trackSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId) {
        for (Analytics service : services) {
            service.trackSubsectionVideosDelete(courseId, subsectionId);
        }
    }

    @Override
    public void trackUndoingSubsectionVideosDelete(@NonNull String courseId, @NonNull String subsectionId) {
        for (Analytics service : services) {
            service.trackUndoingSubsectionVideosDelete(courseId, subsectionId);
        }
    }

    @Override
    public void trackUnitVideoDelete(@NonNull String courseId, @NonNull String unitId) {
        for (Analytics service : services) {
            service.trackUnitVideoDelete(courseId, unitId);
        }
    }

    @Override
    public void trackUndoingUnitVideoDelete(@NonNull String courseId, @NonNull String unitId) {
        for (Analytics service : services) {
            service.trackUndoingUnitVideoDelete(courseId, unitId);
        }
    }

    @Override
    public void trackBulkDownloadSwitchOn(@NonNull String courseId, int totalDownloadableVideos, int remainingDownloadableVideos) {
        for (Analytics service : services) {
            service.trackBulkDownloadSwitchOn(courseId, totalDownloadableVideos, remainingDownloadableVideos);
        }
    }

    @Override
    public void trackBulkDownloadSwitchOff(@NonNull String courseId, int totalDownloadableVideos) {
        for (Analytics service : services) {
            service.trackBulkDownloadSwitchOff(courseId, totalDownloadableVideos);
        }
    }

    @Override
    public void trackSubjectClicked(@NonNull String subjectId) {
        for (Analytics service : services) {
            service.trackSubjectClicked(subjectId);
        }
    }

    @Override
    public void trackExperimentParams(String experimentName, Map<String, String> values) {
        for (Analytics service : services) {
            service.trackExperimentParams(experimentName, values);
        }
    }

    @Override
    public void trackCastDeviceConnectionChanged(@NonNull String eventName, @NonNull String connectionState, @NonNull String playMedium) {
        for (Analytics service : services) {
            service.trackCastDeviceConnectionChanged(eventName, connectionState, playMedium);
        }
    }

    @Override
    public void trackPLSCourseDatesBanner(@NonNull String biValue, @NonNull String courseId,
                                          @NonNull String enrollmentMode, @NonNull String screenName,
                                          @NonNull String bannerType) {
        for (Analytics service : services) {
            service.trackPLSCourseDatesBanner(biValue, courseId, enrollmentMode, screenName, bannerType);
        }
    }

    @Override
    public void trackPLSShiftButtonTapped(@NonNull String courseId, @NonNull String enrollmentMode,
                                          @NonNull String screenName) {
        for (Analytics service : services) {
            service.trackPLSShiftButtonTapped(courseId, enrollmentMode, screenName);
        }
    }

    @Override
    public void trackPLSCourseDatesShift(@NonNull String courseId, @NonNull String enrollmentMode,
                                         @NonNull String screenName, boolean isSuccess) {
        for (Analytics service : services) {
            service.trackPLSCourseDatesShift(courseId, enrollmentMode, screenName, isSuccess);
        }
    }

    @Override
    public void trackValuePropModalView(@NonNull String courseId, @Nullable String assignmentId, @NonNull String screenName) {
        for (Analytics service : services) {
            service.trackValuePropModalView(courseId, assignmentId, screenName);
        }
    }

    @Override
    public void trackValuePropLearnMoreTapped(@NonNull String courseId, @Nullable String assignmentId, @NonNull String screenName) {
        for (Analytics service : services) {
            service.trackValuePropLearnMoreTapped(courseId, assignmentId, screenName);
        }
    }

    @Override
    public void trackLockedContentTapped(@NonNull String courseId, @NonNull String assignmentId) {
        for (Analytics service : services) {
            service.trackLockedContentTapped(courseId, assignmentId);
        }
    }

    @Override
    public void trackUpgradeNowClicked(@NonNull String courseId, @NonNull String price,
                                       @Nullable String componentId, boolean isSelfPaced) {
        for (Analytics service : services) {
            service.trackUpgradeNowClicked(courseId, price, componentId, isSelfPaced);
        }
    }

    @Override
    public void trackValuePropShowMoreLessClicked(@NonNull String courseId, @Nullable String componentId,
                                                  @NonNull String price, boolean isSelfPaced, boolean showMore) {
        for (Analytics service : services) {
            service.trackValuePropShowMoreLessClicked(courseId, componentId, price, isSelfPaced, showMore);
        }
    }

    @Override
    public void trackExploreAllCoursesTapped(String versionName) {
        for (Analytics service : services) {
            service.trackExploreAllCoursesTapped(versionName);
        }
    }

    @Override
    public void trackDatesCourseComponentTapped(@NonNull String courseId, @NonNull String blockId, @NonNull String blockType, @NonNull String link) {
        for (Analytics service : services) {
            service.trackDatesCourseComponentTapped(courseId, blockId, blockType, link);
        }
    }

    @Override
    public void trackUnsupportedComponentTapped(@NonNull String courseId, @NonNull String blockId, @NonNull String link) {
        for (Analytics service : services) {
            service.trackUnsupportedComponentTapped(courseId, blockId, link);
        }
    }

    @Override
    public void trackCourseSectionCelebration(@NonNull String courseId) {
        for (Analytics service : services) {
            service.trackCourseSectionCelebration(courseId);
        }
    }

    @Override
    public void trackCourseCelebrationShareClicked(@NonNull String courseId, @Nullable String socialService) {
        for (Analytics service : services) {
            service.trackCourseCelebrationShareClicked(courseId, socialService);
        }
    }

    @Override
    public void trackResumeCourseBannerTapped(@NonNull String courseId, @NonNull String blockId) {
        for (Analytics service : services) {
            service.trackResumeCourseBannerTapped(courseId, blockId);
        }
    }

    @Override
    public void trackSubsectionViewOnWebTapped(@NonNull String courseId, @NonNull String subsectionId, boolean isSpecialExamInfo) {
        for (Analytics service : services) {
            service.trackSubsectionViewOnWebTapped(courseId, subsectionId, isSpecialExamInfo);
        }
    }

    @Override
    public void trackCalendarEvent(@NonNull String eventName, @NonNull String biValue,
                                   @NonNull String courseId, @NonNull String userType,
                                   @NonNull boolean isSelfPaced, long elapsedTime) {
        for (Analytics service : services) {
            service.trackCalendarEvent(eventName, biValue, courseId, userType, isSelfPaced, elapsedTime);
        }
    }

    @Override
    public void trackOpenInBrowserBannerEvent(@NonNull String eventName, @NonNull String biValue,
                                              @NonNull String userType, @NonNull String courseId,
                                              @NonNull String componentId, @NonNull String componentType,
                                              @NonNull String openedUrl) {
        for (Analytics service : services) {
            service.trackOpenInBrowserBannerEvent(eventName, biValue, userType, courseId,
                    componentId, componentType, openedUrl);
        }
    }

    @Override
    public void trackScreenViewEvent(@NonNull String eventName, @NonNull String screenName) {
        for (Analytics service : services) {
            service.trackScreenViewEvent(eventName, screenName);
        }
    }

    @Override
    public void trackVideoDownloadQualityChanged(@NonNull VideoQuality selectedVideoQuality, @NonNull VideoQuality oldVideoQuality) {
        for (Analytics service : services) {
            service.trackVideoDownloadQualityChanged(selectedVideoQuality, oldVideoQuality);
        }
    }

    @Override
    public void trackEvent(@NonNull String eventName, @NonNull String biValue) {
        for (Analytics service : services) {
            service.trackEvent(eventName, biValue);
        }
    }
}
