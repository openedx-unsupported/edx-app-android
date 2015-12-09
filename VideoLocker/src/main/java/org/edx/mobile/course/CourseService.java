package org.edx.mobile.course;

import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.course.CourseDetail;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public interface CourseService {
    @GET("/api/courses/v1/courses/")
    CourseList getCourseList() throws RetroHttpException;

    @GET("/api/courses/v1/courses/{course_id}")
    CourseDetail getCourseDetail(@Path("course_id") String courseId) throws RetroHttpException;
}
