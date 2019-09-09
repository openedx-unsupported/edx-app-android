package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class SavePeriodTask extends Task<SuccessResponse> {

    private long periodId;
    private List<String> addedIds;
    private List<String> removedIds;

    @Inject
    private TaAPI taAPI;

    public SavePeriodTask(Context context, long periodId, List<String> addedIds, List<String> removedIds) {
        super(context);
        this.periodId = periodId;
        this.addedIds = addedIds;
        this.removedIds = removedIds;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.savePeriod(periodId, addedIds, removedIds).execute().body();
    }
}
