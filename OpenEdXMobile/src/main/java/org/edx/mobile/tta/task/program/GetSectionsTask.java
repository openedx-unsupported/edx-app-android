package org.edx.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Section;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetSectionsTask extends Task<List<Section>> {

    @Inject
    private TaAPI taAPI;

    public GetSectionsTask(Context context) {
        super(context);
    }

    @Override
    public List<Section> call() throws Exception {
        return taAPI.getSections().execute().body();
    }
}
