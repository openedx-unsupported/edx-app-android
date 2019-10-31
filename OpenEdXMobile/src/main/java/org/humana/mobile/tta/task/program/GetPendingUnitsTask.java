package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetPendingUnitsTask extends Task<List<Unit>> {

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
    public List<Unit> call() throws Exception {
        return taAPI.getPendingUnits(programId,sectionId, username, take, skip).execute().body();
    }
}
