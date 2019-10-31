package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetProgramsTask extends Task<List<Program>> {

    @Inject
    private TaAPI taAPI;

    public GetProgramsTask(Context context) {
        super(context);
    }

    @Override
    public List<Program> call() throws Exception {
        return taAPI.getPrograms().execute().body();
    }
}
