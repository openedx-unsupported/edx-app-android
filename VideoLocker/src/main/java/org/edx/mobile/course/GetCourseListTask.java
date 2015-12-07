package org.edx.mobile.course;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class GetCourseListTask extends
        Task<CourseList> {

    @Inject
    private CourseAPI courseAPI;

    final int page;

    public GetCourseListTask(@NonNull Context context, int page) {
        super(context);
        this.page = page;
    }


    public CourseList call() throws Exception {
        return courseAPI.getCourseList(page);
    }
}
