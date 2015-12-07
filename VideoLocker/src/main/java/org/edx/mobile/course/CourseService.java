package org.edx.mobile.course;

import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.course.CourseDetail;

import java.util.List;

import retrofit.http.GET;

public interface CourseService {
    @GET("/api/courses/v1/courses/")
    CourseList getCourseList() throws RetroHttpException;
}
