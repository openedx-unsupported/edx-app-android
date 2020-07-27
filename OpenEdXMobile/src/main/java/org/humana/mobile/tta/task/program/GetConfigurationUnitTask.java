package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.program.UnitConfiguration;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetConfigurationUnitTask extends Task<UnitConfiguration> {


    @Inject
    private TaAPI taAPI;

    public GetConfigurationUnitTask(Context context) {
        super(context);

    }

    @Override
    public UnitConfiguration call() throws Exception {
        return taAPI.getUnitConfiguration().execute().body();
    }
}
