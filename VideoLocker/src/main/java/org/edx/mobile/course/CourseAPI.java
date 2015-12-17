package org.edx.mobile.course;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.http.RetroHttpException;

import retrofit.RestAdapter;
import retrofit.http.Query;

@Singleton
public class CourseAPI {

    private CourseService courseService;

    @Inject
    public CourseAPI(@NonNull RestAdapter restAdapter) {
        courseService = restAdapter.create(CourseService.class);
    }

    public @NonNull CourseList getCourseList(int page) throws RetroHttpException {
        return courseService.getCourseList(page);
    }

    public @NonNull CourseDetail getCourseDetail(@NonNull String courseId) throws RetroHttpException {
        // Empty courseId will return a 200 for a list of course details, instead of a single course
        if (TextUtils.isEmpty(courseId)) throw new IllegalArgumentException();
        return courseService.getCourseDetail(courseId);
    }
}
