package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetPeriodsTask extends Task<List<Period>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId, role;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetPeriodsTask(Context context, List<ProgramFilter> filters, String programId, String sectionId, String role, int take, int skip) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.role = role;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Period> call() throws Exception {
        return taAPI.getPeriods(filters, programId, sectionId, role, take, skip).execute().body();
    }
}
