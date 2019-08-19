package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Period;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetPeriodsTask extends Task<List<Period>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetPeriodsTask(Context context, List<ProgramFilter> filters, String programId, String sectionId, int take, int skip) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Period> call() throws Exception {
        return taAPI.getPeriods(filters, programId, sectionId, take, skip).execute().body();
    }
}
