package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetPendingUnitsTask extends Task<List<CourseComponent>> {

    private String programId, username, sectionId;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetPendingUnitsTask(Context context, String programId,String sectionId, String username, int take, int skip) {
        super(context);
        this.programId = programId;
        this.username = username;
        this.sectionId = sectionId;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<CourseComponent> call() throws Exception {
        return taAPI.getPendingUnits(programId,sectionId, username, take, skip).execute().body();
    }
}
