package org.edx.mobile.course;

import android.support.annotation.NonNull;

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

    public CourseList getCourseList(int page) throws RetroHttpException {
        return courseService.getCourseList(page);
    }
}
