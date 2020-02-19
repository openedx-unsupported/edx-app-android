package org.humana.mobile.tta.task.program;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.model.program.NotificationCountResponse;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetNotificationCountTask extends Task<NotificationCountResponse> {

    @Inject
    private TaAPI taAPI;

    public GetNotificationCountTask(Context context) {
        super(context);
    }

    @Override
    public NotificationCountResponse call() throws Exception {
        return taAPI.getNotificationCount().execute().body();
    }
}
