package org.humana.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.local.db.table.Notification;
import org.humana.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetNotificationsTask extends Task<List<Notification>> {

    private int take, skip;
    private String course_id;

    @Inject
    private TaAPI taAPI;

    public GetNotificationsTask(Context context, int take, int skip, String course_id) {
        super(context);
        this.take = take;
        this.skip = skip;
        this.course_id = course_id;
    }

    @Override
    public List<Notification> call() throws Exception {
        return taAPI.getNotifications(take, skip,course_id).execute().body();
    }
}
