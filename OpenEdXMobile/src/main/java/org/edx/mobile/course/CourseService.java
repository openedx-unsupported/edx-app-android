package org.edx.mobile.course;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.inject.Inject;

import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static org.edx.mobile.http.constants.TimeInterval.HOUR;

public interface CourseService {
    /**
     * A RoboGuice Provider implementation for CourseService.
     */
    class Provider implements com.google.inject.Provider<CourseService> {
        @Inject
        RetrofitProvider retrofitProvider;

        @Override
        public CourseService get() {
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
     * @return Enrolled courses of given user.
     */
    @GET("/api/mobile/v0.5/users/{username}/course_enrollments")
    Call<List<EnrolledCoursesResponse>> getEnrolledCourses(@Path("username") final String username,
                                                           @Query("org") final String org);

    /**
     * @return Enrolled courses of given user, only from the cache.
     */
    @Headers("Cache-Control: only-if-cached, max-stale")
    @GET("/api/mobile/v0.5/users/{username}/course_enrollments")
    Call<List<EnrolledCoursesResponse>> getEnrolledCoursesFromCache(
            @Path("username") final String username,
            @Query("org") final String org);

    /**
     * @return List of videos in a particular course.
     */
    @GET("/api/mobile/v0.5/video_outlines/courses/{course_id}")
    Call<List<VideoResponseModel>> getVideosByCourseId(@Path("course_id") final String courseId);

    @PATCH("/api/mobile/v0.5/users/{username}/course_status_info/{course_id}")
    Call<SyncLastAccessedSubsectionResponse> syncLastAccessedSubsection(
            @Path("username") final String username,
            @Path("course_id") final String courseId,
            @Body final SyncLastAccessedSubsectionBody body);

    @GET("/api/mobile/v0.5/users/{username}/course_status_info/{course_id}")
    Call<SyncLastAccessedSubsectionResponse> getLastAccessedSubsection(
            @Path("username") final String username,
            @Path("course_id") final String courseId);

    @POST("/api/enrollment/v1/enrollment")
    Call<ResponseBody> enrollInACourse(@Body final EnrollBody body);

    @Headers("Cache-Control: max-stale=" + HOUR)
    @GET("/api/courses/v1/blocks/?" +
            "depth=all&" +
            "requested_fields=graded,format,student_view_multi_device&" +
            "student_view_data=video,discussion&" +
            "block_counts=video&" +
            "nav_depth=3")
    Call<CourseStructureV1Model> getCourseStructure(@Query("username") final String username,
                                                    @Query("course_id") final String courseId);

    @Headers("Cache-Control: only-if-cached, max-stale")
    @GET("/api/courses/v1/blocks/?" +
            "depth=all&" +
            "requested_fields=graded,format,student_view_multi_device&" +
            "student_view_data=video,discussion&" +
            "block_counts=video&" +
            "nav_depth=3")
    Call<CourseStructureV1Model> getCourseStructureFromCache(
            @Query("username") final String username,
            @Query("course_id") final String courseId);

    final class SyncLastAccessedSubsectionBody {
        @NonNull
        private final String lastVisitedModuleId;
        @NonNull
        private final Date modificationDate = new Date();

        public SyncLastAccessedSubsectionBody(@NonNull final String lastVisitedModuleId) {
            this.lastVisitedModuleId = lastVisitedModuleId;
        }
    }

    final class EnrollBody {
        @NonNull
        private final CourseDetails courseDetails;

        public EnrollBody(@NonNull final String courseId, final boolean emailOptIn) {
            courseDetails = new CourseDetails(courseId, emailOptIn);
        }

        private static class CourseDetails {
            @NonNull
            private final String courseId;
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
