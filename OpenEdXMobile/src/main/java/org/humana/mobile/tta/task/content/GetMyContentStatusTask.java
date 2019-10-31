package org.humana.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.ContentStatus;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetMyContentStatusTask extends Task<List<ContentStatus>> {

    @Inject
    private TaAPI taAPI;

    public GetMyContentStatusTask(Context context) {
        super(context);
    }

    @Override
    public List<ContentStatus> call() throws Exception {
        return taAPI.getMyContentStatus().execute().body();
    }
}
