package org.edx.mobile.module.serverapi.retrofit;

import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.CourseInfoModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.json.CreateGroupResponse;
import org.edx.mobile.model.json.SuccessResponse;
import org.edx.mobile.module.serverapi.serialization.ShareCourseResult;
import org.edx.mobile.social.SocialMember;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by rohan on 3/16/15.
 */
public interface IRestApi {

    /* GET calls */

    /**
     * Returns user's basic profile information for current active session.
     * @return
     * @throws Exception
     */
    @GET("/api/mobile/v0.5/my_user_info")
    ProfileModel getProfile();

    @GET("/api/mobile/v0.5/video_outlines/courses/{courseId}")
    List<VideoResponseModel> getCourseHierarchy(@Path("courseId") String courseId);

    /**
     * Returns enrolled courses of given user.
     *
     * @return
     * @throws Exception
     */
    @GET("/api/mobile/v0.5/users/{username}/course_enrollments/")
    List<EnrolledCoursesResponse> getEnrolledCourses(@Path("username") String username);

    /**
     * Returns handout for the given course id.
     * @param handoutUrl
     * @return
     * @throws Exception
     */
    @GET("{handoutUrl}")
    HandoutModel getHandout(@Path("handoutUrl") String handoutUrl);

    /**
     * Returns course info object from the given URL.
     * @param courseInfoUrl
     * @return
     * @throws Exception
     */
    @GET("{courseInfoUrl}")
    CourseInfoModel getCourseInfo(@Path("courseInfoUrl") String courseInfoUrl);

    /**
     * Returns list of announcements for the given course id.
     * @param announcementUrl
     * @return
     * @throws Exception
     */
    @GET("{announcementUrl}")
    AnnouncementsModel getAnnouncements(@Path("announcementUrl") String announcementUrl);

    @GET("/api/mobile/v0.5/social/facebook/courses/friends")
    List<EnrolledCoursesResponse> getFriendsCourses();

    @GET("/api/mobile/v0.5/social/facebook/friends/course/{courseId}")
    List<SocialMember> getFriendsInCourse(@Path("courseId") String courseId);

    @GET("/api/mobile/v0.5/settings/preferences/")
    SuccessResponse getUserCourseShareConsent();

    @GET("/api/mobile/v0.5/social/facebook/groups/{groupId}/members")
    List<SocialMember> getGroupMembers(@Path("groupId") String groupId);


    /* POST Calls */

    @POST("/api/mobile/v0.5/video_outlines/courses/{courseId}")
    List<VideoResponseModel> getVideosByCourseId(@Path("courseId") String courseId);

    /**
     * Executes HTTP POST for auth call, and returns response.
     *
     * @return
     * @throws Exception
     */
    @POST("/oauth2/access_token/")
    AuthResponse doLogin(String username, String password);

    /**
     * Resets password for the given email address.
     * @param email
     * @return
     * @throws Exception
     */
    @POST("/password_rest/")
    ResetPasswordResponse doResetPassword(String email);

    @POST("/api/mobile/v0.5/social/facebook/groups/{groupId}/member/")
    SuccessResponse doInviteFriendsToGroup(@Path("groupId") String groupId);

    /**
     *  return of -1 indicates an error
     */
    @POST("/api/mobile/v0.5/social/facebook/groups/")
    CreateGroupResponse doCreateGroup();

    @POST("/api/mobile/v0.5/settings/preferences/")
    ShareCourseResult setUserCourseShareConsent();
}
