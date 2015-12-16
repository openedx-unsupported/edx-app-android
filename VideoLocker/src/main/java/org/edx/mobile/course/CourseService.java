package org.edx.mobile.course;

import org.edx.mobile.http.RetroHttpException;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface CourseService {
    @GET("/api/courses/v1/courses/")
    CourseList getCourseList(@Query("page") int page) throws RetroHttpException;

    @GET("/api/courses/v1/courses/{course_id}")
    CourseDetail getCourseDetail(@Path("course_id") String courseId) throws RetroHttpException;
}
