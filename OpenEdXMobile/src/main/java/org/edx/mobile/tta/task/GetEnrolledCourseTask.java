package org.edx.mobile.tta.task;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.remote.api.TaAPI;

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
