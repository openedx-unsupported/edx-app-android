package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Unit;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUnitsTask extends Task<List<Unit>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId, role;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetUnitsTask(Context context, List<ProgramFilter> filters, String programId, String sectionId,
                        String role, int take, int skip) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.role = role;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Unit> call() throws Exception {
        return taAPI.getUnits(filters, programId, sectionId, role, take, skip).execute().body();
    }

}
