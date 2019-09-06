package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.TaAPI;


public class GetCourseComponentTask extends Task<CourseComponent> {

    private String unit_id;

    @Inject
    private TaAPI taAPI;

    public GetCourseComponentTask(Context context, String unit_id) {
        super(context);
        this.unit_id = unit_id;

    }

    @Override
    public CourseComponent call() throws Exception {
        return taAPI.getCourseComponentUnits(unit_id).execute().body();
    }
}
