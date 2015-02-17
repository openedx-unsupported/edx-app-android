package org.edx.mobile.module.serverapi;

import android.os.Bundle;

import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SocialLoginResponse;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.social.SocialMember;

import java.util.List;
import java.util.Map;

/**
 * Created by rohan on 2/6/15.
 */
public interface IApi {

    /**
     * Resets password for the given email address.
     * @param email
     * @return
     * @throws Exception
     */
    ResetPasswordResponse doResetPassword(String email) throws Exception;

    /**
     * Executes HTTP POST for auth call, and returns response.
     *
     * @return
     * @throws Exception
     */
    AuthResponse doLogin(String username, String password) throws Exception;

    SocialLoginResponse doLoginByFacebook(String accessToken) throws Exception;

    SocialLoginResponse doLoginByGoogle(String accessToken) throws Exception;

    /**
     * Downloads transcript file from given url.
     * @param url
     * @return
     * @throws Exception
     */
    String doDownloadTranscript(String url) throws Exception;

    /**
     * Creates new account.
     * @param parameters
     * @return
     * @throws Exception
     */
    RegisterResponse doRegister(Bundle parameters) throws Exception;

    boolean doEnrollInACourse(String courseId, boolean email_opt_in) throws Exception;

    String doDownloadRegistrationDescription() throws Exception;

    SyncLastAccessedSubsectionResponse doSyncLastAccessedSubsection(String courseId,
                                                                    String lastVisitedModuleId) throws Exception;


    boolean doInviteFriendsToGroup(long[] toInvite, long groupId, String oauthToken) throws Exception;

    /**
     *  return of -1 indicates an error
     */
    long doCreateGroup(String name, String description, boolean privacy, long adminId, String socialToken) throws Exception;

    /**
     * Returns user's basic profile information for current active session.
     * @return
     * @throws Exception
     */
    ProfileModel getProfile() throws Exception;

    /**
     * Returns entire course hierarchy.
     *
     * @param courseId
     * @return
     * @throws Exception
     */
    Map<String, SectionEntry> getCourseHierarchy(String courseId) throws Exception;

    /**
     * Returns entire course hierarchy.
     *
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache) throws Exception;

    /**
     * Returns lecture model for given course id, chapter name and lecture name combination.
     * @param courseId
     * @param chapterName
     * @param lectureName
     * @return
     * @throws Exception
     */
    LectureModel getLecture(String courseId, String chapterName, String lectureName) throws Exception;

    /**
     * Returns video model for given course id and video id.
     * @param courseId
     * @param videoId
     * @return
     * @throws Exception
     */
    VideoResponseModel getVideoById(String courseId, String videoId) throws Exception;

    /**
     * Returns any (mostly first video always) one video {@link VideoResponseModel}
     * object from subsection identified by
     * given module id (subsectionId).
     * @param courseId
     * @param subsectionId
     * @return
     * @throws Exception
     */
    VideoResponseModel getSubsectionById(String courseId, String subsectionId) throws Exception;

    /**
     * Returns UnitUrl for given course id and video id.
     * @param courseId
     * @param videoId
     * @return
     * @throws Exception
     */
    String getUnitUrlByVideoById(String courseId, String videoId) throws Exception;

    /**
     * Returns enrolled courses of given user.
     *
     * @return
     * @throws Exception
     */
    List<EnrolledCoursesResponse> getEnrolledCourses() throws Exception;

    /**
     * Returns course identified by given id from cache, null if not course is found.
     * @param courseId
     * @return
     */
    CourseEntry getCourseById(String courseId);

    /**
     * Returns enrolled courses of given user.
     *
     * @param fetchFromCache
     * @return
     * @throws Exception
     */
    List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception;

    /**
     * Returns list of videos in a particular course.
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    List<VideoResponseModel> getVideosByCourseId(String courseId, boolean preferCache) throws Exception;

    /**
     * Returns handout for the given course id.
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    HandoutModel getHandout(String url, boolean preferCache) throws Exception;

    /**
     * Returns course info object from the given URL.
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    CourseInfoModel getCourseInfo(String url, boolean preferCache) throws Exception;

    /**
     * Returns list of announcements for the given course id.
     * @param url
     * @param preferCache
     * @return
     * @throws Exception
     */
    List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache)
            throws Exception;

    /**
     * Returns Stream object from the given URL.
     * @param url
     * @return
     * @throws Exception
     */
    CourseInfoModel getSrtStream(String url) throws Exception;

    /**
     * Returns Transcript of a given Video.
     *
     * @param
     * @return TranscriptModel
     * @throws Exception
     */
    TranscriptModel getTranscriptsOfVideo(String enrollmentId, String videoId);

    /**
     * Returns list of videos for a particular URL.
     * @param courseId
     * @param preferCache
     * @return
     * @throws Exception
     */
    List<VideoResponseModel> getVideosByURL(String courseId, String videoUrl, boolean preferCache)
            throws Exception;

    List<EnrolledCoursesResponse> getFriendsCourses(String oauthToken) throws Exception;

    List<EnrolledCoursesResponse> getFriendsCourses(boolean preferCache, String oauthToken) throws Exception;

    List<SocialMember> getFriendsInCourse(String courseId, String oauthToken) throws Exception;

    List<SocialMember> getFriendsInCourse(boolean preferCache, String courseId, String oauthToken) throws Exception;

    void setUserCourseShareConsent(boolean consent) throws Exception;

    boolean getUserCourseShareConsent() throws Exception;

    List<SocialMember> getGroupMembers(boolean preferCache, long groupId) throws Exception;

    /**
     * Returns chapter model and the subsequent sections and videos in organized manner from cache.
     * @param courseId
     * @param chapter
     * @return
     */
    List<SectionItemInterface> getLiveOrganizedVideosByChapter(String courseId, String chapter);

    SyncLastAccessedSubsectionResponse getLastAccessedSubsection(String courseId) throws Exception;

    /**
     * Reads registration description from assets and return Model representation of it.
     * @return
     * @throws Exception
     */
    RegistrationDescription getRegistrationDescription() throws Exception;
}
