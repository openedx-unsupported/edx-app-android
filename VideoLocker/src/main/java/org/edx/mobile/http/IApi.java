package org.edx.mobile.http;

import android.os.Bundle;

import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.LastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.module.registration.model.RegistrationDescription;
import org.edx.mobile.social.SocialMember;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO - we won't need this api when we fully migrate the code to okhttp
 */
@Deprecated
public interface IApi {
    ResetPasswordResponse resetPassword(String emailId)
            throws Exception;

    AuthResponse auth(String username, String password)
            throws Exception;

    ProfileModel getProfile() throws Exception;

    List<EnrolledCoursesResponse> getEnrolledCourses()
            throws Exception;

    EnrolledCoursesResponse getCourseById(String courseId);

    List<EnrolledCoursesResponse> getEnrolledCourses(boolean fetchFromCache) throws Exception;

    HandoutModel getHandout(String url, boolean fetchFromCache) throws Exception;

    List<AnnouncementsModel> getAnnouncement(String url, boolean preferCache)
            throws Exception;


    String downloadTranscript(String url)
            throws Exception;

    List<SocialMember> getFriendsInCourse(boolean preferCache, String courseId, String oauthToken) throws Exception;

    boolean inviteFriendsToGroup(long[] toInvite, long groupId, String oauthToken) throws Exception;

    long createGroup(String name, String description, boolean privacy, long adminId, String socialToken) throws Exception;

    boolean setUserCourseShareConsent(boolean consent) throws Exception;

    boolean getUserCourseShareConsent() throws Exception;

    List<SocialMember> getGroupMembers(boolean preferCache, long groupId) throws Exception;

    AuthResponse loginByFacebook(String accessToken) throws Exception;

    AuthResponse loginByGoogle(String accessToken) throws Exception;

    RegisterResponse register(Bundle parameters)
            throws Exception;

    RegistrationDescription getRegistrationDescription() throws Exception;

    Boolean enrollInACourse(String courseId, boolean email_opt_in) throws Exception;

    List<HttpCookie> getSessionExchangeCookie() throws Exception;

    @Deprecated
    VideoResponseModel getVideoById(String courseId, String videoId)
        throws Exception;

    @Deprecated
    Map<String, SectionEntry> getCourseHierarchy(String courseId, boolean preferCache) throws Exception;

    @Deprecated
    ArrayList<SectionItemInterface> getLiveOrganizedVideosByChapter
            (String courseId, String chapter);

    HttpManager.HttpResult getCourseStructure(HttpRequestDelegate delegate) throws Exception;
}
