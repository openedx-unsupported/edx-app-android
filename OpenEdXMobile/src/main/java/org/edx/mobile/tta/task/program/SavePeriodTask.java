package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;
import java.util.Map;

public class SavePeriodTask extends Task<SuccessResponse> {

    private long periodId;
    private List<String> addedIds;
    private List<String> removedIds;
    private Map<String, Long> proposedDateModified;
    private Map<String, Long> proposedDateAdded;

    @Inject
    private TaAPI taAPI;

    public SavePeriodTask(Context context, long periodId, List<String> addedIds, List<String> removedIds,
                          Map<String, Long> proposedDateModified, Map<String, Long> proposedDateAdded) {
        super(context);
        this.periodId = periodId;
        this.addedIds = addedIds;
        this.removedIds = removedIds;
        this.proposedDateModified = proposedDateModified;
        this.proposedDateAdded = proposedDateAdded;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.savePeriod(periodId, addedIds, removedIds, proposedDateModified, proposedDateAdded)
                .execute().body();
    }
}
