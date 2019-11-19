package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUnitsTask extends Task<List<Unit>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId, role,student_username;
    private int take, skip;
    private long period_id, startDateTime, endDateTime;

    @Inject
    private TaAPI taAPI;

    public GetUnitsTask(Context context, List<ProgramFilter> filters, String programId, String sectionId,
                        String role,long period_id, int take, int skip, String student_username,Long startDateTime,
                        Long endDateTime) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.period_id = period_id;
        this.role = role;
        this.take = take;
        this.skip = skip;
        this.student_username = student_username;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    @Override
    public List<Unit> call() throws Exception {
        return taAPI.getUnits(filters, programId, sectionId, role,student_username,period_id, take, skip, startDateTime, endDateTime).execute().body();
    }

}
