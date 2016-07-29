package org.edx.mobile.http;

import org.edx.mobile.http.model.EnrollmentRequestBody;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

import static org.edx.mobile.http.ApiConstants.COURSE_ID;
import static org.edx.mobile.http.ApiConstants.URL_COURSE_ENROLLMENTS;
import static org.edx.mobile.http.ApiConstants.URL_COURSE_OUTLINE;
import static org.edx.mobile.http.ApiConstants.URL_ENROLLMENT;
import static org.edx.mobile.http.ApiConstants.URL_LAST_ACCESS_FOR_COURSE;
import static org.edx.mobile.http.ApiConstants.URL_VIDEO_OUTLINE;
import static org.edx.mobile.http.ApiConstants.USER_NAME;

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
    List<EnrolledCoursesResponse> getEnrolledCourses(@Path(USER_NAME) String username);

    @Headers("Cache-Control: no-cache")
    @GET(URL_COURSE_ENROLLMENTS)
    List<EnrolledCoursesResponse> getEnrolledCoursesNoCache(@Path(USER_NAME) String username);

    /* POST Calls */

    @POST(URL_VIDEO_OUTLINE)
    List<VideoResponseModel> getVideosByCourseId(@Path(COURSE_ID) String courseId);

    @PUT(URL_LAST_ACCESS_FOR_COURSE)
    SyncLastAccessedSubsectionResponse syncLastAccessedSubsection(@Body EnrollmentRequestBody.LastAccessRequestBody body,
                                                                  @Path(USER_NAME) String username,
                                                                  @Path(COURSE_ID) String courseId);

    @GET(URL_LAST_ACCESS_FOR_COURSE)
    SyncLastAccessedSubsectionResponse getLastAccessedSubsection(@Path(USER_NAME) String username,
                                                                 @Path(COURSE_ID) String courseId);


    @POST(URL_ENROLLMENT)
    String enrollACourse(@Body EnrollmentRequestBody body);

}
