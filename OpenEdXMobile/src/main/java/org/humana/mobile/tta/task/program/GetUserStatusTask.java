package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUserStatusTask extends Task<List<Unit>> {

    private List<ProgramFilter> filters;
    private String programId, sectionId, role, studentName;
    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetUserStatusTask(Context context, List<ProgramFilter> filters, String programId, String sectionId,
                        String role, String studentName, int take, int skip) {
        super(context);
        this.filters = filters;
        this.programId = programId;
        this.sectionId = sectionId;
        this.studentName = studentName;
        this.role = role;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Unit> call() throws Exception {
        return taAPI.getUserUnits(filters, programId, sectionId, role,studentName, take, skip).execute().body();
    }

}