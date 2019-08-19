package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetProgramFiltersTask extends Task<List<ProgramFilter>> {

    @Inject
    private TaAPI taAPI;

    public GetProgramFiltersTask(Context context) {
        super(context);
    }

    @Override
    public List<ProgramFilter> call() throws Exception {
        return taAPI.getProgramFilters().execute().body();
    }
}
