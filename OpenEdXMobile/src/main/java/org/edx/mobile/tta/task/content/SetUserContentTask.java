package org.edx.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.ContentStatus;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class SetUserContentTask extends Task<List<ContentStatus>> {

    private List<ContentStatus> statuses;

    @Inject
    private TaAPI taAPI;

    public SetUserContentTask(Context context, List<ContentStatus> statuses) {
        super(context);
        this.statuses = statuses;
    }

    @Override
    public List<ContentStatus> call() throws Exception {
        return taAPI.setUserContent(statuses).execute().body();
    }
}
