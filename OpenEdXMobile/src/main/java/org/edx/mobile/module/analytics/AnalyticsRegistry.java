package org.edx.mobile.module.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Singleton;

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
                                String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackVideoPause(videoId, currentTime, courseId, unitUrl);
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
                                  String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackVideoPlaying(videoId, currentTime, courseId, unitUrl);
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
                                      String courseId, String unitUrl) {
        for (Analytics service : services) {
            service.trackVideoOrientation(videoId, currentTime, isLandscape, courseId, unitUrl);
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
}
