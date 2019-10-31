package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class SetProposedDateTask extends Task<SuccessResponse> {

    private String programId, sectionId;
    private long periodId, proposedDate;
    private String unitId;

    @Inject
    private TaAPI taAPI;

    public SetProposedDateTask(Context context, String programId, String sectionId, long periodId,
                               long proposedDate, String unitId) {
        super(context);
        this.programId = programId;
        this.sectionId = sectionId;
        this.periodId = periodId;
        this.proposedDate = proposedDate;
        this.unitId = unitId;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.setProposedDate(programId, sectionId, proposedDate, periodId, unitId).execute().body();
    }
}
