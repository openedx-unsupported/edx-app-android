package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class RejectUnitTask extends Task<SuccessResponse> {

    private String unitId, username, remarks;
    private int rating;
    @Inject
    private TaAPI taAPI;

    public RejectUnitTask(Context context, String unitId, String username, String remarks, int rating) {
        super(context);
        this.unitId = unitId;
        this.username = username;
        this.remarks = remarks;
        this.rating = rating;
    }

    @Override
    public SuccessResponse call() throws Exception {
        return taAPI.rejectUnit(unitId, username, remarks, rating).execute().body();
    }
}
