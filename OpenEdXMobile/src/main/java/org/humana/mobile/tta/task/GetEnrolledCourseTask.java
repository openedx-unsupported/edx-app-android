package org.humana.mobile.tta.task;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.course.CourseAPI;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.task.Task;

import java.util.List;

public class GetEnrolledCourseTask extends Task<List<EnrolledCoursesResponse>> {

   private String org;
   private String username;

    @Inject
    private CourseAPI api;

    public GetEnrolledCourseTask(Context context, String mOrg,String mUsername) {
        super(context);
        this.org = mOrg;
        this.username=mUsername;
    }

    @Override
    public List<EnrolledCoursesResponse> call() throws Exception {
        return api.getEnrolledCourses().execute().body();
    }
}
