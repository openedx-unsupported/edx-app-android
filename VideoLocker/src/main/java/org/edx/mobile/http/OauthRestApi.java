package org.edx.mobile.http;

import org.edx.mobile.http.model.EnrollmentRequestBody;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    Call<List<VideoResponseModel>> getCourseHierarchy(@Path(COURSE_ID) String courseId);

    @Headers("Cache-Control: no-cache")
    @GET(URL_COURSE_OUTLINE)
    Call<String> getCourseOutlineNoCache(@Query("course_id") String courseId,
                                         @Query("user") String username,
                                         @Query("requested_fields") String fields,
                                         @Query("student_view_data") String blockJson,
                                         @Query("block_counts") String blockCount);

    @GET(URL_COURSE_OUTLINE)
    Call<String> getCourseOutline(@Query("course_id") String courseId,
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
    Call<List<EnrolledCoursesResponse>> getEnrolledCourses(@Path(USER_NAME) String username);

    @Headers("Cache-Control: no-cache")
    @GET(URL_COURSE_ENROLLMENTS)
    Call<List<EnrolledCoursesResponse>> getEnrolledCoursesNoCache(@Path(USER_NAME) String username);

    /* POST Calls */

    @POST(URL_VIDEO_OUTLINE)
    Call<List<VideoResponseModel>> getVideosByCourseId(@Path(COURSE_ID) String courseId);

    @PUT(URL_LAST_ACCESS_FOR_COURSE)
    Call<SyncLastAccessedSubsectionResponse> syncLastAccessedSubsection(@Body EnrollmentRequestBody.LastAccessRequestBody body,
                                                                        @Path(USER_NAME) String username,
                                                                        @Path(COURSE_ID) String courseId);

    @GET(URL_LAST_ACCESS_FOR_COURSE)
    Call<SyncLastAccessedSubsectionResponse> getLastAccessedSubsection(@Path(USER_NAME) String username,
                                                                       @Path(COURSE_ID) String courseId);


    @POST(URL_ENROLLMENT)
    Call<String> enrollACourse(@Body EnrollmentRequestBody body);

}
