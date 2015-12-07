package org.edx.mobile.course;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class GetCourseListTask extends
        Task<CourseList> {

    @Inject
    private CourseAPI courseAPI;

    public GetCourseListTask(@NonNull Context context) {
        super(context);
    }


    public CourseList call() throws Exception {
        return courseAPI.getCourseList();
    }
}
