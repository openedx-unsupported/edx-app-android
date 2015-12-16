package org.edx.mobile.course;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class GetCourseDetailTask extends
        Task<CourseDetail> {

    @NonNull final String courseId;

    @Inject
    private CourseAPI courseAPI;

    public GetCourseDetailTask(@NonNull Context context, @NonNull String courseId) {
        super(context);
        this.courseId = courseId;
    }

    public CourseDetail call() throws Exception {
        if(courseId!=null){
            return courseAPI.getCourseDetail(courseId);
        }
        return null;
    }
}
