package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.CurricullamModel;
import org.humana.mobile.tta.data.remote.api.TaAPI;

public class GetCurricullamTask extends Task<CurricullamModel> {

    @Inject
    private TaAPI taAPI;
    private String program_id;

    public GetCurricullamTask(Context context, String program_id) {
        super(context);
        this.program_id = program_id;
    }

    @Override
    public CurricullamModel call() throws Exception {
        return taAPI.getCurricullam(program_id).execute().body();
    }
}
