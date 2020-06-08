package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.program.UnitPublish;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUnitPublishTask extends Task<UnitPublish> {

    private String unitId;

    @Inject
    private TaAPI taAPI;

    public GetUnitPublishTask(Context context, String unitId) {
        super(context);
        this.unitId = unitId;

    }

    @Override
    public UnitPublish call() throws Exception {
        return taAPI.getUnitPublish(unitId).execute().body();
    }
}
