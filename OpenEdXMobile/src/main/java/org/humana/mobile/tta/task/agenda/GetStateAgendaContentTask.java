package org.humana.mobile.tta.task.agenda;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetStateAgendaContentTask extends Task<List<Content>> {

    private long sourceId;

    private long list_id;

    @Inject
    private TaAPI taAPI;

    public GetStateAgendaContentTask(Context context, long sourceId, long list_id) {
        super(context);
        this.sourceId = sourceId;
        this.list_id = list_id;
    }

    @Override
    public List<Content> call() throws Exception {
        return taAPI.getStateAgendaContent(sourceId, list_id).execute().body();
    }
}
