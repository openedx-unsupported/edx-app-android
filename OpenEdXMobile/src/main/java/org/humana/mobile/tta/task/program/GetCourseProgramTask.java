package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.CourseProgram;
import org.humana.mobile.tta.data.model.program.UnitConfiguration;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GetCourseProgramTask extends Task<CourseProgram> {


    @Inject
    private TaAPI taAPI;

    public GetCourseProgramTask(Context context) {
        super(context);

    }

    @Override
    public CourseProgram call() throws Exception {
        return taAPI.getProgramCourse().execute().body();
    }
}
