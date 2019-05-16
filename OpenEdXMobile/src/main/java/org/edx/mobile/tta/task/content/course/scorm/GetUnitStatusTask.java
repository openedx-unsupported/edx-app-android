package org.edx.mobile.tta.task.content.course.scorm;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.UnitStatus;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUnitStatusTask extends Task<List<UnitStatus>> {

    private String courseId;

    @Inject
    private TaAPI taAPI;

    public GetUnitStatusTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public List<UnitStatus> call() throws Exception {
        return taAPI.getUnitStatus(courseId).execute().body();
    }
}
