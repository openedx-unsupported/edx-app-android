package org.edx.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class IsContentMyAgendaTask extends Task<StatusResponse> {

    private long contentId;

    @Inject
    private TaAPI taAPI;

    public IsContentMyAgendaTask(Context context, long contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    public StatusResponse call() throws Exception {
        return taAPI.isContentMyAgenda(contentId).execute().body();
    }
}
