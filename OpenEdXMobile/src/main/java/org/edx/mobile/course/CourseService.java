package org.edx.mobile.course;

import com.google.inject.Inject;

import org.edx.mobile.model.Page;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CourseService {
    /**
     * A RoboGuice Provider implementation for CourseService.
     */
    class Provider implements com.google.inject.Provider<CourseService> {
        @Inject
        private Retrofit retrofit;

        @Override
        public CourseService get() {
            return retrofit.create(CourseService.class);
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
    Call<Page<CourseDetail>> getCourseList(@Query("username") String username,
                                           @Query("mobile") boolean mobile,
                                           @Query("org") String org,
                                           @Query("page") int page);

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
    Call<CourseDetail> getCourseDetail(@Path("course_id") String courseId, @Query("username") String username);
}
