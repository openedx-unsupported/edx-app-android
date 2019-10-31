package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Section;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetSectionsTask extends Task<List<Section>> {

    private String programId;

    @Inject
    private TaAPI taAPI;

    public GetSectionsTask(Context context, String programId) {
        super(context);
        this.programId = programId;
    }

    @Override
    public List<Section> call() throws Exception {
        return taAPI.getSections(programId).execute().body();
    }
}
