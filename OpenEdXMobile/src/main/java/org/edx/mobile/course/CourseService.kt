package org.edx.mobile.course

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ResponseBody
import org.edx.mobile.http.constants.ApiConstants
import org.edx.mobile.http.provider.RetrofitProvider
import org.edx.mobile.model.Page
import org.edx.mobile.model.api.AppConfig
import org.edx.mobile.model.api.CourseComponentStatusResponse
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.api.EnrollmentResponse
import org.edx.mobile.model.course.BlocksCompletionBody
import org.edx.mobile.model.course.CourseBannerInfoModel
import org.edx.mobile.model.course.CourseDates
import org.edx.mobile.model.course.CourseDetail
import org.edx.mobile.model.course.CourseStatus
import org.edx.mobile.model.course.CourseStructureV1Model
import org.edx.mobile.model.course.EnrollBody
import org.edx.mobile.model.course.ResetCourseDates
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Singleton

interface CourseService {

    @Module
    @InstallIn(SingletonComponent::class)
    class Provider {
        @Singleton
        @Provides
        operator fun get(retrofitProvider: RetrofitProvider): CourseService {
            return retrofitProvider.withOfflineCache.create(CourseService::class.java)
        }
    }

    /**
     * This API fetches the enrolled courses of a user under [EnrolledCoursesResponse] along with
     * the App Configuration of different features under [AppConfig] based on different enrollment
     * API versions.
     *
     * @param cacheControlHeaderParam Cache-Control directive, to serve from the local cache upon
     *                                encountering a server or network error.
     * @param username                The username of the specified user whose visible courses we
     *                                want to see.
     * @param enrollmentsApiVersion   The version of the enrollments API we want to fetch from the
     *                                server.
     * @param org                     If specified, only courses with the relevant organization code
     *                                are returned.
     */
    @GET("/api/mobile/{api_version}/users/{username}/course_enrollments")
    fun getEnrolledCourses(
        @Header("Cache-Control") cacheControlHeaderParam: String?,
        @Path("username") username: String,
        @Path("api_version") enrollmentsApiVersion: String,
        @Query("org") org: String?
    ): Call<EnrollmentResponse>

    @POST("/api/enrollment/v1/enrollment")
    fun enrollInACourse(@Body body: EnrollBody): Call<ResponseBody>

    /**
     * @param username The username of the specified user whose visible courses we want to see. The
     *                 username is not required only if the API is requested by an Anonymous user.
     * @param mobile   If specified, only visible `CourseOverview` objects that are designated as
     *                 mobile_available are returned.
     * @param org      If specified, only courses with the relevant organization code are returned.
     * @param page     Which page to fetch. If not given, defaults to page 1
     */
    @GET("/api/courses/v1/courses/")
    fun getCourseList(
        @Query("username") username: String?,
        @Query("mobile") mobile: Boolean,
        @Query("org") org: String?,
        @Query("page") page: Int
    ): Call<Page<CourseDetail>>

    /**
     * @param courseId If specified, visible `CourseOverview` objects are filtered such that only
     *                 those belonging to the organization with the provided org code (e.g.,
     *                 "HarvardX") are returned.
     *                 Case-insensitive.
     * @param username The username of the specified user whose visible courses we want to see. The
     *                 username is not required only if the API is requested by an Anonymous user.
     */
    @GET("/api/courses/v1/courses/{course_id}")
    fun getCourseDetail(
        @Path("course_id") courseId: String,
        @Query("username") username: String
    ): Call<CourseDetail>

    @GET("/api/courses/{api_version}/blocks/?" + ApiConstants.COURSE_STRUCTURE_REQUIRED_PATH)
    fun getCourseStructure(
        @Header("Cache-Control") cacheControlHeaderParam: String,
        @Path("api_version") blocksApiVersion: String,
        @Query("username") username: String,
        @Query("course_id") courseId: String
    ): Call<CourseStructureV1Model>

    @GET("/api/course_home/v1/dates/{course_key}")
    fun getCourseDates(@Path("course_key") courseId: String): Call<CourseDates>

    @POST("/api/course_experience/v1/reset_course_deadlines")
    fun resetCourseDates(@Body courseBody: Map<String, String>): Call<ResetCourseDates>

    @GET("/api/course_experience/v1/course_deadlines_info/{course_key}")
    fun getCourseBannerInfo(@Path("course_key") courseId: String): Call<CourseBannerInfoModel>

    @GET("/api/courseware/course/{course_id}")
    fun getCourseStatus(@Path("course_id") courseId: String): Call<CourseStatus>

    @GET("/api/mobile/v1/users/{username}/course_status_info/{course_id}")
    fun getCourseStatusInfo(
        @Path("username") username: String,
        @Path("course_id") courseId: String
    ): Call<CourseComponentStatusResponse>

    @Headers("Cache-Control: no-cache")
    @GET("/api/experiments/v0/custom/REV-934")
    fun getCourseUpgradeStatus(@Query("course_id") courseId: String): Call<CourseUpgradeResponse>

    @POST("/api/completion/v1/completion-batch")
    fun markBlocksCompletion(@Body completionBody: BlocksCompletionBody?): Call<JSONObject>

    @POST("/api/courseware/celebration/{course_id}")
    fun updateCoursewareCelebration(
        @Path("course_id") courseId: String,
        @Body courseBody: Map<String, Boolean>
    ): Call<Void>
}
