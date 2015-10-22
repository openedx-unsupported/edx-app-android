package org.edx.mobile.http;

import org.edx.mobile.http.model.EnrollmentRequestBody;
import org.edx.mobile.http.serialization.ShareCourseResult;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.json.CreateGroupResponse;
import org.edx.mobile.model.json.SuccessResponse;
import org.edx.mobile.social.SocialMember;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

import static org.edx.mobile.http.ApiConstants.*;

/**
 * we group all the mobile endpoints which require oauth token together
 */
public interface OauthRestApi {

    /* GET calls */

    /**
     * Returns user's basic profile information for current active session.
     * @return
     * @throws Exception
     */
    @GET(URL_MY_USER_INFO)
    ProfileModel getProfile();

    @GET(URL_USER_INFO)
    ProfileModel getProfile(@Path(USER_NAME) String username);

    @GET(URL_VIDEO_OUTLINE)
    List<VideoResponseModel> getCourseHierarchy(@Path(COURSE_ID) String courseId);

    @Headers("Cache-Control: no-cache")
    @GET(URL_COURSE_OUTLINE)
    String getCourseOutlineNoCache(@Query("course_id") String courseId,
                                   @Query("user") String username,
                                   @Query("requested_fields") String fields,
                                   @Query("student_view_data") String blockJson,
                                   @Query("block_counts") String blockCount);

    @GET(URL_COURSE_OUTLINE)
    String getCourseOutline(@Query("course_id") String courseId,
                            @Query("user") String username,
                            @Query("requested_fields") String fields,
                            @Query("student_view_data") String blockJson,
                            @Query("block_counts") String blockCount);

    /**
     * Returns enrolled courses of given user.
     *
     * @return
     * @throws Exception
     */
    @GET(URL_COURSE_ENROLLMENTS)
    List<EnrolledCoursesResponse> getEnrolledCourses(@Path(USER_NAME) String username);

    @Headers("Cache-Control: no-cache")
    @GET(URL_COURSE_ENROLLMENTS)
    List<EnrolledCoursesResponse> getEnrolledCoursesNoCache(@Path(USER_NAME) String username);

    @Headers("Cache-Control: no-cache")
    @GET(URL_FB_FRIENDS_COURSE)
    List<EnrolledCoursesResponse> getFriendsCoursesNoCache(@Query("format") String format, @Query("oauth_token") String oauthToken);

    @GET(URL_FB_FRIENDS_COURSE)
    List<EnrolledCoursesResponse> getFriendsCourses(@Query("format") String format, @Query("oauth_token") String oauthToken);

    @Headers("Cache-Control: no-cache")
    @GET(URL_FB_FRIENDS_IN_COURSE)
    List<SocialMember> getFriendsInCourseNoCache(@Path(COURSE_ID) String courseId, @Query("format") String format, @Query("oauth_token") String oauthToken);

    @GET(URL_FB_FRIENDS_IN_COURSE)
    List<SocialMember> getFriendsInCourse(@Path(COURSE_ID) String courseId, @Query("format") String format, @Query("oauth_token") String oauthToken);

    @GET(URL_USER_COURSE_SHARE_CONSENT_GET)
    SuccessResponse getUserCourseShareConsent();

    @Headers("Cache-Control: no-cache")
    @GET(URL_FB_GROUP_MEMBER)
    List<SocialMember> getGroupMembersNoCache(@Path(GROUP_ID) String groupId);

    @GET(URL_FB_GROUP_MEMBER)
    List<SocialMember> getGroupMembers(@Path(GROUP_ID) String groupId);

    /* POST Calls */

    @POST(URL_VIDEO_OUTLINE)
    List<VideoResponseModel> getVideosByCourseId(@Path(COURSE_ID) String courseId);


    @FormUrlEncoded
    @POST(URL_FB_INVITE_TO_GROUP)
    SuccessResponse doInviteFriendsToGroup(@Field("format") String format,
                                           @Field("member_ids") String memberIds,
                                           @Field("oauth_token") String oauthToken,
                                           @Path(GROUP_ID) String groupId);

    /**
     *  return of -1 indicates an error
     */
    @FormUrlEncoded
    @POST( URL_FB_CREATE_GROUPS)
    CreateGroupResponse doCreateGroup(@Field("format") String format,
                                      @Field("name") String name,
                                      @Field("description") String description,
                                      @Field("privacy") String privacy,
                                      @Field("admin-id") String adminId,
                                      @Field("oauth_token") String oauthToken);

    @FormUrlEncoded
    @POST(URL_USER_COURSE_SHARE_CONSENT)
    ShareCourseResult setUserCourseShareConsent(@Field("format") String format,
                                                @Field("share_with_facebook_friends") String shareWithFriends);

    @PUT(URL_LAST_ACCESS_FOR_COURSE)
    SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(@Body EnrollmentRequestBody.LastAccessRequestBody body,
                                                                  @Path(USER_NAME) String username,
                                                                  @Path(COURSE_ID) String courseId);

    @GET(URL_LAST_ACCESS_FOR_COURSE)
    SyncLastAccessedSubsectionResponse getLastAccessedSubsection( @Path(USER_NAME) String username,
                                                                  @Path(COURSE_ID) String courseId);


    @POST(URL_ENROLLMENT)
    String enrollACourse(@Body EnrollmentRequestBody body);

}