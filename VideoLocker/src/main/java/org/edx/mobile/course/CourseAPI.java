package org.edx.mobile.course;

import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.http.RetroHttpException;

import retrofit.RestAdapter;

@Singleton
public class CourseAPI {

    private CourseService courseService;

    @Inject
    public CourseAPI(@NonNull RestAdapter restAdapter) {
        courseService = restAdapter.create(CourseService.class);
    }

    public CourseList getCourseList() throws RetroHttpException {
        return courseService.getCourseList();
    }

    public CourseDetail getCourseDetail(String courseId) throws RetroHttpException {
        return courseService.getCourseDetail(courseId);
    }
}
