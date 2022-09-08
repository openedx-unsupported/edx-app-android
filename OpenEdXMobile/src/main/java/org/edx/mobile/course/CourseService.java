package org.edx.mobile.course;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.CourseComponentStatusResponse;
import org.edx.mobile.model.api.CourseUpgradeResponse;
import org.edx.mobile.model.api.EnrollmentResponse;
import org.edx.mobile.model.course.CourseBannerInfoModel;
import org.edx.mobile.model.course.CourseDates;
import org.edx.mobile.model.course.CourseDetail;
import org.edx.mobile.model.course.CourseStatus;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.ResetCourseDates;
import org.edx.mobile.view.common.TaskProgressCallback;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.HashMap;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CourseService {
    /**
     * A Provider implementation for CourseService.
     */
    @Module
    @InstallIn(SingletonComponent.class)
    class Provider {

        @Singleton
        @Provides
        public CourseService get(@NonNull RetrofitProvider retrofitProvider) {
            return retrofitProvider.getWithOfflineCache().create(CourseService.class);
        }
    }

    /**
     * @param username (optional):
     *                 The username of the specified user whose visible courses we
     *                 want to see. The username is not required only if the API is
     *                 requested by an Anonymous user.
     * @param mobile   (optional):
     *                 If specified, only visible `CourseOverview` objects that are
     *                 designated as mobile_available are returned.
     * @param org      (optional):
     *                 If specified, only courses with the relevant organization
     *                 code are returned.
     * @param page     (optional):
     *                 Which page to fetch. If not given, defaults to page 1
     */
    @GET("/api/courses/v1/courses/")
    Call<Page<CourseDetail>> getCourseList(@Query("username") final String username,
                                           @Query("mobile") final boolean mobile,
                                           @Query("org") final String org,
                                           @Query("page") final int page);

    /**
     * @param courseId (optional):
     *                 If specified, visible `CourseOverview` objects are filtered
     *                 such that only those belonging to the organization with the
     *                 provided org code (e.g., "HarvardX") are returned.
     *                 Case-insensitive.
     * @param username (optional):
     *                 The username of the specified user whose visible courses we
     *                 want to see. The username is not required only if the API is
     *                 requested by an Anonymous user.
     */
    @GET("/api/courses/v1/courses/{course_id}")
    Call<CourseDetail> getCourseDetail(@Path("course_id") final String courseId,
                                       @Query("username") final String username);

    /**
     * @return Upgrade status for the given course.
     */
    @Headers("Cache-Control: no-cache")
    @GET("/api/experiments/v0/custom/REV-934")
    Call<CourseUpgradeResponse> getCourseUpgradeStatus(@Query("course_id") String courseId);

    /**
     * @return Enrolled courses of given user.
     */
    @GET("/api/mobile/{api_version}/users/{username}/course_enrollments")
    Call<EnrollmentResponse> getEnrolledCourses(
            @Header("Cache-Control") String cacheControlHeaderParam,
            @Path("username") final String username,
            @Path("api_version") String enrollmentsApiVersion,
            @Query("org") final String org);

    /**
     * @return Enrolled courses of given user, only from the cache.
     */
    @Headers("Cache-Control: only-if-cached, max-stale")
    @GET("/api/mobile/{api_version}/users/{username}/course_enrollments")
    Call<EnrollmentResponse> getEnrolledCoursesFromCache(
            @Path("username") final String username,
            @Path("api_version") String enrollmentsApiVersion,
            @Query("org") final String org);

    @GET("/api/mobile/v1/users/{username}/course_status_info/{course_id}")
    Call<CourseComponentStatusResponse> getCourseStatusInfo(
            @Path("username") final String username,
            @Path("course_id") final String courseId);

    @POST("/api/enrollment/v1/enrollment")
    Call<ResponseBody> enrollInACourse(@Body final EnrollBody body);

    @GET("/api/courses/{api_version}/blocks/?" +
            "depth=all&" +
            "requested_fields=contains_gated_content,show_gated_sections,special_exam_info,graded,format,student_view_multi_device,due,completion&" +
            "student_view_data=video,discussion&" +
            "block_counts=video&" +
            "nav_depth=3")
    Call<CourseStructureV1Model> getCourseStructure(
            @Header("Cache-Control") String cacheControlHeaderParam,
            @Path("api_version") String blocksApiVersion,
            @Query("username") final String username,
            @Query("course_id") final String courseId);

    @POST("/api/completion/v1/completion-batch")
    Call<JSONObject> markBlocksCompletion(@Body BlocksCompletionBody completionBody);

    @GET("/api/course_home/v1/dates/{course_key}")
    Call<CourseDates> getCourseDates(@Path("course_key") String courseId);

    @GET("/api/course_experience/v1/course_deadlines_info/{course_key}")
    Call<CourseBannerInfoModel> getCourseBannerInfo(@Path("course_key") String courseId);

    @POST("/api/course_experience/v1/reset_course_deadlines")
    Call<ResetCourseDates> resetCourseDates(@Body HashMap<String, String> courseBody);

    @GET("/api/courseware/course/{course_id}")
    Call<CourseStatus> getCourseStatus(@Path("course_id") final String courseId);

    @POST("/api/courseware/celebration/{course_id}")
    Call<Void> updateCoursewareCelebration(@Path("course_id") final String courseId, @Body HashMap<String, Boolean> courseBody);

    final class BlocksCompletionBody {

        @NonNull
        @SerializedName("username")
        String username;

        @NonNull
        @SerializedName("course_key")
        String courseKey;

        @NonNull
        HashMap<String, String> blocks = new HashMap<>();

        public BlocksCompletionBody(@NonNull String username, @NonNull String courseKey, @NonNull String[] blockIds) {
            this.username = username;
            this.courseKey = courseKey;
            for (int index = 0; index < blockIds.length; index++) {
                blocks.put(blockIds[index], "1");
            }
        }
    }

    final class EnrollBody {
        @NonNull
        @SerializedName("course_details")
        private final CourseDetails courseDetails;

        public EnrollBody(@NonNull final String courseId, final boolean emailOptIn) {
            courseDetails = new CourseDetails(courseId, emailOptIn);
        }

        private static class CourseDetails {
            @NonNull
            @SerializedName("course_id")
            private final String courseId;

            @SerializedName("email_opt_in")
            private final boolean emailOptIn;

            CourseDetails(@NonNull final String courseId, final boolean emailOptIn) {
                this.courseId = courseId;
                this.emailOptIn = emailOptIn;
            }
        }
    }

    class EnrollCallback extends ErrorHandlingCallback<ResponseBody> {
        public EnrollCallback(@NonNull final Context context) {
            super(context);
        }

        public EnrollCallback(@NonNull final Context context,
                              @Nullable final TaskProgressCallback progressCallback) {
            super(context, progressCallback);
        }

        @Override
        protected void onResponse(@NonNull final ResponseBody responseBody) {
            EventBus.getDefault().post(new EnrolledInCourseEvent());
        }
    }
}
