package org.humana.mobile.tta.task.content.course;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class UserEnrollmentCourseFromCacheTask extends Task<EnrolledCoursesResponse> {

    private String courseId;

    @Inject
    private TaAPI taAPI;

    public UserEnrollmentCourseFromCacheTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public EnrolledCoursesResponse call() throws Exception {
        return taAPI.userEnrollmentCourseFromCache(courseId).execute().body();
    }

}
