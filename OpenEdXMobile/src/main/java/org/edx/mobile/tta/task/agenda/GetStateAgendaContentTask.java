package org.edx.mobile.tta.task.agenda;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetStateAgendaContentTask extends Task<List<Content>> {

    private long sourseId;

    @Inject
    private TaAPI taAPI;

    public GetStateAgendaContentTask(Context context, long sourseId) {
        super(context);
        this.sourseId = sourseId;
    }

    @Override
    public List<Content> call() throws Exception {
        return taAPI.getStateAgendaContent(sourseId).execute().body();
    }
}
