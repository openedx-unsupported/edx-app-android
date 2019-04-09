package org.edx.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.CountResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class UpdateNotificationsTask extends Task<CountResponse> {

    private List<String> notificationIds;

    @Inject
    private TaAPI taAPI;

    public UpdateNotificationsTask(Context context, List<String> notificationIds) {
        super(context);
        this.notificationIds = notificationIds;
    }

    @Override
    public CountResponse call() throws Exception {
        return taAPI.updateNotifications(notificationIds).execute().body();
    }
}
