package org.edx.mobile.module.analytics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import org.edx.mobile.util.images.ShareUtils;

import java.util.Map;

/**
 * Created by rohan on 2/8/15.
 *
 * This implementation of {@link org.edx.mobile.module.analytics.ISegment} does nothing.
 * None of the methods are implemented. This class is significant to let the app call
 * tracking methods on {@link org.edx.mobile.module.analytics.ISegment}, however,
 * data is actually not captured.
 */
public class ISegmentEmptyImpl implements ISegment {

    @Override
    public Traits identifyUser(String userID, String email, String username) {
        return null;
    }

    @Override
    public Properties trackVideoPlaying(String videoId, Double currentTime, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackVideoPause(String videoId, Double currentTime, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackVideoStop(String videoId, Double currentTime, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackShowTranscript(String videoId, Double currentTime, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackHideTranscript(String videoId, Double currentTime, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackVideoLoading(String videoId, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackVideoSeek(String videoId, Double oldTime, Double newTime, String courseId, String unitUrl, Boolean skipSeek) {
        return null;
    }

    @Override
    public void resetIdentifyUser() {

    }

    @Override
    public Properties trackScreenView(@NonNull String screenName) {
        return null;
    }

    @Override
    public Properties trackScreenView(@NonNull String screenName, @Nullable String courseId,
                                      @Nullable String action) {
        return null;
    }

    @Override
    public Properties trackScreenView(@NonNull String screenName, @Nullable String courseId,
                                      @Nullable String action,
                                      @Nullable Map<String, String> values) {
        return null;
    }

    @Override
    public Properties trackDownloadComplete(String videoId, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackOpenInBrowser(String url) {
        return null;
    }

    @Override
    public Properties trackSectionBulkVideoDownload(String enrollmentId, String section, long videoCount) {
        return null;
    }

    @Override
    public Properties trackSubSectionBulkVideoDownload(String section, String subSection, String enrollmentId, long videoCount) {
        return null;
    }

    @Override
    public Properties trackUserLogin(String method) {
        return null;
    }

    @Override
    public Properties trackUserLogout() {
        return null;
    }

    @Override
    public Properties trackTranscriptLanguage(String videoId, Double currentTime, String lang, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackSingleVideoDownload(String videoId, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackVideoOrientation(String videoId, Double currentTime, boolean isLandscape, String courseId, String unitUrl) {
        return null;
    }

    @Override
    public Properties trackDiscoverCoursesClicked() {
        return null;
    }

    @Override
    public Properties trackExploreSubjectsClicked() {
        return null;
    }

    @Override
    public Properties trackUserSignUpForAccount() {
        return null;
    }

    @Override
    public Properties trackUserFindsCourses() {
        return null;
    }

    @Override
    public Properties trackCreateAccountClicked(String appVersion, String source) {
            return null;
    }

    @Override
    public Properties trackEnrollClicked(String courseId, boolean email_opt_in) {
        return null;
    }

    @Override
    public Properties trackNotificationReceived(@Nullable String courseId){
        return null;
    }

    @Override
    public Properties trackNotificationTapped(@Nullable String courseId){
        return null;
    }

    @Override
    public void setTracker(ISegmentTracker tracker) {
    }

    @Override
    public Properties trackUserConnectionSpeed(String connectionType, float connectionSpeed) {
        return null;
    }

    @Override
    public Properties courseDetailShared(@NonNull String courseId, @NonNull String aboutUrl, @NonNull ShareUtils.ShareType shareType) {
        return null;
    }

    @Override
    public Properties certificateShared(@NonNull String courseId, @NonNull String certificateUrl, @NonNull ShareUtils.ShareType shareType) {
        return null;
    }

    @Override
    public Properties trackCourseOutlineMode(boolean isVideoMode) {
        return null;
    }

    @Override
    public Properties trackCourseComponentViewed(String blockId, String courseId) {
        return null;
    }

    @Override
    public Properties trackOpenInBrowser(String blockId, String courseId, boolean isSupported) {
        return null;
    }

    @Override
    public Properties trackProfileViewed(@NonNull String username) {
        return null;
    }

    @Override
    public Properties trackProfilePhotoSet(boolean fromCamera) {
        return null;
    }
}
