package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class UpdatePeriodTask extends Task<SuccessResponse> {

    private String programId, sectionId, periodId, periodName;
    private long startDate, endDate;

    @Inject
    private TaAPI taAPI;

    public UpdatePeriodTask(Context context, String programId, String sectionId,
                            String periodId, String periodName, long startDate, long endDate) {
        super(context);
        this.programId = programId;
        this.sectionId = sectionId;
        this.periodId = periodId;
        this.periodName = periodName;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.updatePeriods(programId, sectionId, periodId, periodName, startDate, endDate).execute().body();
    }
}
