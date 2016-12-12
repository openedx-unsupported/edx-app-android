package org.edx.mobile.module.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.module.analytics.IEvents;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.images.ShareUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simplifies creating tracking logs on multiple services without a lot of code change
 *
 * @author EDRAAK | ahmedjazzar
 * @since 11/16/16
 */

@Singleton
public class EventsTracker implements IEvents {

    List<IEvents> services;

    @Inject
    EventsTracker(Config config) {
        this.services = config.getEventsTrackers();
    }

    public List<Object> trackScreenView(String screenName)    {
        return trackScreenView(screenName, null, null);
    }

    public List<Object> trackScreenView(@NonNull String screenName, @Nullable String courseId,
                                        @Nullable String action)    {
        return trackScreenView(screenName, courseId, action, null);
    }

    @Override
    public List<Object> trackScreenView(@NonNull String screenName, @Nullable String courseId,
                                        @Nullable String action, @Nullable Map<String, String> values)    {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackScreenView(screenName, courseId, action, values));
        }
        return returns;
    }

    @Override
    public List<Object> trackOpenInBrowser(String url) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackOpenInBrowser(url));
        }
        return returns;
    }

    @Override
    public List<Object> trackOpenInBrowser(String blockId, String courseId, boolean isSupported)    {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackOpenInBrowser(blockId, courseId, isSupported));
        }
        return returns;
    }

    @Override
    public List<Object> trackDownloadComplete(String videoId, String courseId, String unitUrl)    {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackDownloadComplete(videoId, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackUserFindsCourses()  {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackUserFindsCourses());
        }
        return returns;
    }

    @Override
    public List<Object> trackUserConnectionSpeed(String connectionType, float connectionSpeed)   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackUserConnectionSpeed(connectionType, connectionSpeed));
        }
        return returns;
    }

    public List<Object> certificateShared(String courseId, String certificateURL,
                                          ShareUtils.ShareType method)   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    certificateShared(courseId, certificateURL, getShareTypeValue(method)));
        }
        return returns;
    }

    public List<Object> courseDetailShared(String courseId, String shareText,
                                           ShareUtils.ShareType method)  {
        return courseDetailShared(courseId, shareText, getShareTypeValue(method));
    }

    @Override
    public List<Object> certificateShared(@NonNull String courseId, @NonNull String certificateUrl,
                                          @NonNull String shareType) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.certificateShared(courseId, certificateUrl, shareType));
        }
        return returns;
    }

    @Override
    public List<Object> courseDetailShared(@NonNull String courseId, @NonNull String aboutUrl,
                                           @NonNull String shareType) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.courseDetailShared(courseId, aboutUrl, shareType));
        }
        return returns;
    }

    public List<Object> trackUserLogin(String method)  {
        return trackUserLogin(method, true);
    }

    @Override
    public List<Object> trackUserLogin(String method, boolean didSucceed)  {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackUserLogin(method, didSucceed));
        }
        return returns;
    }

    @Override
    public List<Object> trackUserRegister(String method, boolean didSucceed)  {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackUserRegister(method, didSucceed));
        }
        return returns;
    }

    @Override
    public List<Object> trackUserLogout()   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(service.trackUserLogout());
        }
        return returns;
    }

    @Override
    public List<Object> trackEnrollClicked(String courseId, boolean email_opt_in)   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackEnrollClicked(courseId, email_opt_in));
        }
        return returns;
    }

    @Override
    public List<Object> trackNotificationReceived(@Nullable String courseId) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(service.trackNotificationReceived(courseId));
        }
        return returns;
    }

    @Override
    public List<Object> trackVideoPause(String videoId, Double currentTime,
                                        String courseId, String unitUrl)   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackVideoPause(videoId, currentTime, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackVideoLoading(String videoId, String courseId, String unitUrl) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackVideoLoading(videoId, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackVideoPlaying(String videoId, Double currentTime,
                                          String courseId, String unitUrl)  {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackVideoPlaying(videoId, currentTime, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackVideoStop(String videoId, Double currentTime, String courseId, String unitUrl){
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackVideoStop(videoId, currentTime, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackVideoOrientation(String videoId, Double currentTime, boolean isLandscape,
                                              String courseId, String unitUrl) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackVideoOrientation(videoId, currentTime, isLandscape, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackTranscriptLanguage(String videoId, Double currentTime,
                                                String lang, String courseId, String unitUrl)   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackTranscriptLanguage(videoId, currentTime, lang, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackHideTranscript(String videoId, Double currentTime,
                                            String courseId, String unitUrl)    {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackHideTranscript(videoId, currentTime, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackShowTranscript(String videoId, Double currentTime,
                                            String courseId, String unitUrl)    {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackShowTranscript(videoId, currentTime, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackVideoSeek(String videoId, Double oldTime, Double newTime,
                                       String courseId, String unitUrl, Boolean skipSeek)   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackVideoSeek(videoId, oldTime, newTime, courseId, unitUrl, skipSeek));
        }
        return returns;
    }

    @Override
    public List<Object> trackSingleVideoDownload(String videoId, String courseId, String unitUrl)   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackSingleVideoDownload(videoId, courseId, unitUrl));
        }
        return returns;
    }

    @Override
    public List<Object> trackUserSignUpForAccount() {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackUserSignUpForAccount());
        }
        return returns;
    }

    @Override
    public List<Object> trackCourseOutlineMode(boolean isVideoMode) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackCourseOutlineMode(isVideoMode));
        }
        return returns;
    }

    @Override
    public List<Object> trackCourseComponentViewed(String blockId, String courseId) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackCourseComponentViewed(blockId, courseId));
        }
        return returns;
    }

    @Override
    public List<Object> trackDiscoverCoursesClicked()   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackDiscoverCoursesClicked());
        }
        return returns;
    }

    @Override
    public List<Object> trackExploreSubjectsClicked()   {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(service.trackExploreSubjectsClicked());
        }
        return returns;
    }

    @Override
    public List<Object> trackCreateAccountClicked(String appVersion, String source) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackCreateAccountClicked(appVersion, source));
        }
        return returns;
    }

    @Override
    public List<Object> trackNotificationTapped(@Nullable String courseId)  {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackNotificationTapped(courseId));
        }
        return returns;
    }

    @Override
    public List<Object> trackProfileViewed(@NonNull String username)    {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackProfileViewed(username));
        }
        return returns;
    }

    @Override
    public List<Object> trackSectionBulkVideoDownload(String enrollmentId, String section,
                                                      long videoCount) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackSectionBulkVideoDownload(enrollmentId, section, videoCount));
        }
        return returns;
    }

    @Override
    public List<Object> trackSubSectionBulkVideoDownload(String section, String subSection,
                                                         String enrollmentId, long videoCount) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackSubSectionBulkVideoDownload(section, subSection, enrollmentId, videoCount));
        }
        return returns;
    }

    @Override
    public List<Object> trackProfilePhotoSet(boolean fromCamera) {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            returns.add(
                    service.trackProfilePhotoSet(fromCamera));
        }
        return returns;
    }

    @Override
    public List<Map<String, Object>> identifyUser(String userID, String email, String username) {
        List<Map<String, Object>> userIdentifiers = new ArrayList<>();
        for (IEvents service: services) {
            userIdentifiers.add(
                    (Map<String, Object>) service.identifyUser(userID, email, username));
        }
        return userIdentifiers;
    }

    /**
     * This resets the Identify user once the user has logged out
     */
    @Override
    public void resetIdentifyUser() {
        List<Object> returns = new ArrayList<>();
        for (IEvents service: services) {
            service.resetIdentifyUser();
        }
    }

    public String getShareTypeValue(@NonNull ShareUtils.ShareType shareType) {
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

