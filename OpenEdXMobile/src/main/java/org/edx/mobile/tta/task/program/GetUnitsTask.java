package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUnitsTask extends Task<List<CourseComponent>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetUnitsTask(Context context, List<ProgramFilter> filters, String programId, String sectionId, int take, int skip) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<CourseComponent> call() throws Exception {
        return taAPI.getUnits(filters, programId, sectionId, take, skip).execute().body();
    }

}
