package org.edx.mobile.course;

import org.edx.mobile.http.RetroHttpException;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface CourseService {
    /**
     * @param username (optional):
     *                 The username of the specified user whose visible courses we
     *                 want to see. The username is not required only if the API is
     *                 requested by an Anonymous user.
     * @param mobile   (optional):
     *                 If specified, only visible `CourseOverview` objects that are
     *                 designated as mobile_available are returned.
     * @param page     (optional):
     *                 Which page to fetch. If not given, defaults to page 1
     */
    @GET("/api/courses/v1/courses/")
    CourseList getCourseList(@Query("username") String username,
                             @Query("mobile") boolean mobile,
                             @Query("page") int page) throws RetroHttpException;

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
    CourseDetail getCourseDetail(@Path("course_id") String courseId, @Query("username") String username) throws RetroHttpException;
}
