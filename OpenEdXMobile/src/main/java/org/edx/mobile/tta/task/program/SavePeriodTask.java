package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class SavePeriodTask extends Task<SuccessResponse> {

    private long periodId;
    private List<CharSequence> unitIds;

    @Inject
    private TaAPI taAPI;

    public SavePeriodTask(Context context, long periodId, List<CharSequence> unitIds) {
        super(context);
        this.periodId = periodId;
        this.unitIds = unitIds;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.savePeriod(periodId, unitIds).execute().body();
    }
}
