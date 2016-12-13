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
    List<EnrolledCoursesResponse> getEnrolledCourses(@Path(USER_NAME) String username, @Query("org") String org);

    @Headers("Cache-Control: no-cache")
    @GET(URL_COURSE_ENROLLMENTS)
    List<EnrolledCoursesResponse> getEnrolledCoursesNoCache(@Path(USER_NAME) String username, @Query("org") String org);

    /* POST Calls */

    @POST(URL_VIDEO_OUTLINE)
    List<VideoResponseModel> getVideosByCourseId(@Path(COURSE_ID) String courseId);

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
