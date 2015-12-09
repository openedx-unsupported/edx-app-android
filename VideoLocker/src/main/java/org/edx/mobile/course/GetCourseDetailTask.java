package org.edx.mobile.course;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;

public abstract class GetCourseDetailTask extends
        Task<CourseDetail> {

    String courseId;

    @Inject
    private CourseAPI courseAPI;

    public GetCourseDetailTask(@NonNull Context context, @NonNull String courseId) {
        super(context);
        this.courseId = courseId;
    }

    public CourseDetail call() throws Exception {
        try {
            if(courseId!=null){
                return courseAPI.getCourseDetail(courseId);
            }
        } catch (Exception ex) {
            // TODO What do we want to if course_id is not passed in, the api will return a 200 course list
        }
        return null;
    }
}
