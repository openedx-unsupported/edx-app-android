package org.edx.mobile.tta.task.agenda;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.AgendaList;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetStateAgendaCountTask extends Task<AgendaList> {

    @Inject
    private TaAPI taAPI;

    public GetStateAgendaCountTask(Context context) {
        super(context);
    }

    @Override
    public AgendaList call() throws Exception {
        return taAPI.getStateAgendaCount().execute().body();
    }
}