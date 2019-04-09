package org.edx.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.local.db.table.Notification;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class CreateNotificationsTask extends Task<List<Notification>> {

    private List<Notification> notifications;

    @Inject
    private TaAPI taAPI;

    public CreateNotificationsTask(Context context, List<Notification> notifications) {
        super(context);
        this.notifications = notifications;
    }

    @Override
    public List<Notification> call() throws Exception {
        return taAPI.createNotifications(notifications).execute().body();
    }
}
