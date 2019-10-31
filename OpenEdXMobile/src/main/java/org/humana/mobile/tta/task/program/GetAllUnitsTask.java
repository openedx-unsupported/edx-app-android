package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetAllUnitsTask extends Task<List<Unit>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId, searchText;
    private long periodId;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetAllUnitsTask(Context context, List<ProgramFilter> filters, String programId, String sectionId, String searchText, long periodId, int take, int skip) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.take = take;
        this.skip = skip;
        this.searchText = searchText;
        this.periodId = periodId;
    }

    @Override
    public List<Unit> call() throws Exception {
        return taAPI.getAllUnits(filters, programId, sectionId, searchText, periodId, take, skip).execute().body();
    }

}
