package org.edx.mobile.tta.task.agenda;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetMyAgendaContentTask extends Task<List<Content>> {

    private long sourceId;

    @Inject
    private TaAPI taAPI;

    public GetMyAgendaContentTask(Context context, long sourceId) {
        super(context);
        this.sourceId = sourceId;
    }

    @Override
    public List<Content> call() throws Exception {
        return taAPI.getMyAgendaContent(sourceId).execute().body();
    }
}
